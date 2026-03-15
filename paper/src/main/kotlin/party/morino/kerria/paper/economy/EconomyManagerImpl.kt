package party.morino.kerria.paper.economy

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.paper.database.repository.AccountRepository
import java.math.BigDecimal
import java.util.UUID

/**
 * 経済操作のビジネスロジックを実装するクラス
 *
 * アトミックなDB操作で入金・出金・送金を行い、ログ記録の結果も確認する。
 */
class EconomyManagerImpl : EconomyManager, KoinComponent {
    private val accountRepository: AccountRepository by inject()
    private val logManager: LogManager by inject()
    private val currencyManager: CurrencyManager by inject()

    override fun deposit(accountId: UUID, currencyId: Int, amount: BigDecimal): Either<KerriaError, BigDecimal> {
        // 金額バリデーション
        if (amount <= BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(amount, "Amount must be positive").left()
        }
        // 通貨が存在するか確認
        currencyManager.getCurrency(currencyId).onLeft { return it.left() }

        return runCatching {
            transaction {
                // アカウントの存在確認
                accountRepository.findById(accountId)
                    ?: return@transaction KerriaError.PlayerNotFound(accountId.toString()).left()

                // 残高行を確保し、アトミックに加算
                accountRepository.ensureBalanceRow(accountId, currencyId)
                accountRepository.addBalance(accountId, currencyId, amount)

                // 取引ログを記録し、エラーがあれば伝搬する
                logManager.logTransaction(accountId, accountId, currencyId, amount)
                    .onLeft { return@transaction it.left() }

                // 更新後の残高を返す
                accountRepository.getBalance(accountId, currencyId).right()
            }
        }.getOrElse { e ->
            KerriaError.DatabaseError("Deposit failed: ${e.message}", e).left()
        }
    }

    override fun withdraw(accountId: UUID, currencyId: Int, amount: BigDecimal): Either<KerriaError, BigDecimal> {
        // 金額バリデーション
        if (amount <= BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(amount, "Amount must be positive").left()
        }
        // 通貨が存在するか確認
        currencyManager.getCurrency(currencyId).onLeft { return it.left() }

        return runCatching {
            transaction {
                // アカウントの存在確認
                accountRepository.findById(accountId)
                    ?: return@transaction KerriaError.PlayerNotFound(accountId.toString()).left()

                // アトミックに減算（残高チェック付き）
                val rows = accountRepository.subtractBalance(accountId, currencyId, amount)
                if (rows == 0) {
                    val currentBalance = accountRepository.getBalance(accountId, currencyId)
                    return@transaction KerriaError.InsufficientBalance(
                        required = amount,
                        actual = currentBalance,
                    ).left()
                }

                // 取引ログを記録
                logManager.logTransaction(accountId, accountId, currencyId, amount.negate())
                    .onLeft { return@transaction it.left() }

                // 更新後の残高を返す
                accountRepository.getBalance(accountId, currencyId).right()
            }
        }.getOrElse { e ->
            KerriaError.DatabaseError("Withdraw failed: ${e.message}", e).left()
        }
    }

    override fun transfer(
        fromAccountId: UUID,
        toAccountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
    ): Either<KerriaError, Unit> {
        // 金額バリデーション
        if (amount <= BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(amount, "Amount must be positive").left()
        }
        // 通貨が存在するか確認
        currencyManager.getCurrency(currencyId).onLeft { return it.left() }

        return runCatching {
            transaction {
                // 送金元の存在確認
                accountRepository.findById(fromAccountId)
                    ?: return@transaction KerriaError.PlayerNotFound(fromAccountId.toString()).left()
                // 送金先の存在確認
                accountRepository.findById(toAccountId)
                    ?: return@transaction KerriaError.PlayerNotFound(toAccountId.toString()).left()

                // 送金元からアトミックに減算（残高チェック付き）
                val rows = accountRepository.subtractBalance(fromAccountId, currencyId, amount)
                if (rows == 0) {
                    val currentBalance = accountRepository.getBalance(fromAccountId, currencyId)
                    return@transaction KerriaError.InsufficientBalance(
                        required = amount,
                        actual = currentBalance,
                    ).left()
                }

                // 送金先に加算
                accountRepository.ensureBalanceRow(toAccountId, currencyId)
                accountRepository.addBalance(toAccountId, currencyId, amount)

                // 取引ログを記録
                logManager.logTransaction(fromAccountId, toAccountId, currencyId, amount)
                    .onLeft { return@transaction it.left() }

                Unit.right()
            }
        }.getOrElse { e ->
            KerriaError.DatabaseError("Transfer failed: ${e.message}", e).left()
        }
    }
}
