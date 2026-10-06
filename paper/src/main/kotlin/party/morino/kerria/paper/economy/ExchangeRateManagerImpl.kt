package party.morino.kerria.paper.economy

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.ExchangeRate
import party.morino.kerria.api.economy.ExchangeRateManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.common.database.repository.AccountRepository
import party.morino.kerria.common.database.repository.ExchangeRateRepository
import party.morino.kerria.paper.event.KerriaTransactionEvent
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/**
 * 為替レート管理機能の実装クラス
 *
 * レートの取得・設定と通貨変換を行う。
 */
class ExchangeRateManagerImpl : ExchangeRateManager, KoinComponent {
    private val exchangeRateRepository: ExchangeRateRepository by inject()
    private val accountRepository: AccountRepository by inject()
    private val currencyManager: CurrencyManager by inject()
    private val logManager: LogManager by inject()

    override fun getRate(fromCurrencyId: Int, toCurrencyId: Int): Either<KerriaError, BigDecimal> = runCatching {
        transaction {
            exchangeRateRepository.findRate(fromCurrencyId, toCurrencyId)?.right()
                ?: KerriaError.CurrencyNotFound("Exchange rate not found: $fromCurrencyId -> $toCurrencyId").left()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to get exchange rate: ${e.message}", e).left()
    }

    override fun setRate(
        fromCurrencyId: Int,
        toCurrencyId: Int,
        rate: BigDecimal,
    ): Either<KerriaError, Unit> {
        // バリデーション
        if (rate <= BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(rate, "Rate must be positive").left()
        }
        if (fromCurrencyId == toCurrencyId) {
            return KerriaError.InvalidAmount(rate, "Cannot set rate for same currency").left()
        }
        // 通貨の存在確認
        currencyManager.getCurrency(fromCurrencyId).onLeft { return it.left() }
        currencyManager.getCurrency(toCurrencyId).onLeft { return it.left() }

        return runCatching {
            transaction {
                exchangeRateRepository.setRate(fromCurrencyId, toCurrencyId, rate)
                Unit.right()
            }
        }.getOrElse { e ->
            KerriaError.DatabaseError("Failed to set exchange rate: ${e.message}", e).left()
        }
    }

    override fun deleteRate(fromCurrencyId: Int, toCurrencyId: Int): Either<KerriaError, Unit> = runCatching {
        transaction {
            // 削除件数が 0 の場合は、存在しないレートとして getRate と同じエラーを返す
            val deleted = exchangeRateRepository.deleteRate(fromCurrencyId, toCurrencyId)
            if (deleted > 0) {
                Unit.right()
            } else {
                KerriaError.CurrencyNotFound("Exchange rate not found: $fromCurrencyId -> $toCurrencyId").left()
            }
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to delete exchange rate: ${e.message}", e).left()
    }

    override fun getAllRates(): Either<KerriaError, List<ExchangeRate>> = runCatching {
        transaction {
            // リポジトリの Triple 表現を API のデータクラスへ変換する
            exchangeRateRepository.findAll()
                .map { (from, to, rate) -> ExchangeRate(from, to, rate) }
                .right()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to get exchange rates: ${e.message}", e).left()
    }

    override fun convert(
        accountId: UUID,
        fromCurrencyId: Int,
        toCurrencyId: Int,
        amount: BigDecimal,
    ): Either<KerriaError, BigDecimal> {
        // 金額バリデーション
        if (amount <= BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(amount, "Amount must be positive").left()
        }
        if (fromCurrencyId == toCurrencyId) {
            return KerriaError.InvalidAmount(amount, "Cannot convert to same currency").left()
        }

        // レートを取得
        val rate = getRate(fromCurrencyId, toCurrencyId).getOrNull()
            ?: return KerriaError.CurrencyNotFound("Exchange rate not found: $fromCurrencyId -> $toCurrencyId").left()

        // 変換先通貨の小数桁数を取得
        val toCurrency = currencyManager.getCurrency(toCurrencyId).getOrNull()
            ?: return KerriaError.CurrencyNotFound(toCurrencyId.toString()).left()

        // 変換額を計算
        val convertedAmount = amount.multiply(rate).setScale(toCurrency.fractionalDigits, RoundingMode.HALF_UP)

        // イベント発火（キャンセル可能）。変換元通貨と金額を通知する。
        // トランザクション開始前に一度だけ発火することで、操作の途中でキャンセルされて
        // 出金だけが確定してしまう状態を防ぐ。
        val event = KerriaTransactionEvent(
            KerriaTransactionEvent.TransactionType.CONVERT,
            accountId, accountId, fromCurrencyId, amount, "Kerria",
        )
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return KerriaError.InvalidAmount(amount, "Transaction cancelled by event").left()
        }

        // 出金・入金・ログを単一トランザクションで実行し、原子性を保証する。
        // いずれかが失敗すると throw してロールバックするため、出金だけが確定して
        // 資金が消失するバグ（旧実装の withdraw/deposit 二重トランザクション）を防ぐ。
        return runCatching {
            transaction {
                // アカウントの存在確認
                accountRepository.findById(accountId)
                    ?: throw KerriaError.AccountNotFound(accountId.toString())

                // 変換元通貨からアトミックに減算（残高チェック付き）
                val rows = accountRepository.subtractBalance(accountId, fromCurrencyId, amount)
                if (rows == 0) {
                    val currentBalance = accountRepository.getBalance(accountId, fromCurrencyId)
                    throw KerriaError.InsufficientBalance(
                        required = amount,
                        actual = currentBalance,
                    )
                }

                // 変換先通貨にアトミックに加算
                accountRepository.ensureBalanceRow(accountId, toCurrencyId)
                accountRepository.addBalance(accountId, toCurrencyId, convertedAmount)

                // 取引ログを記録（失敗時は throw してロールバック）。
                // 出金側（変換元・負値）と入金側（変換先・正値）の両方を残す。
                logManager.logTransaction(
                    accountId, accountId, fromCurrencyId, amount.negate(), "Currency conversion", "Kerria",
                ).onLeft { throw it }
                logManager.logTransaction(
                    accountId, accountId, toCurrencyId, convertedAmount, "Currency conversion", "Kerria",
                ).onLeft { throw it }

                convertedAmount.right()
            }
        }.getOrElse { e ->
            when (e) {
                is KerriaError -> e.left()
                else -> KerriaError.DatabaseError("Currency conversion failed: ${e.message}", e).left()
            }
        }
    }
}
