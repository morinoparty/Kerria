package party.morino.kerria.paper.economy

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.economy.ExchangeRateManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.paper.database.repository.AccountRepository
import party.morino.kerria.paper.database.repository.ExchangeRateRepository
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
    private val economyManager: EconomyManager by inject()

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

        // 変換元通貨から出金
        economyManager.withdraw(accountId, fromCurrencyId, amount, "Currency conversion", "Kerria")
            .onLeft { return it.left() }

        // 変換先通貨に入金
        economyManager.deposit(accountId, toCurrencyId, convertedAmount, "Currency conversion", "Kerria")
            .onLeft { return it.left() }

        return convertedAmount.right()
    }
}
