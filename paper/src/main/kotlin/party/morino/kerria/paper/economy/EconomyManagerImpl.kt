package party.morino.kerria.paper.economy

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.paper.database.repository.AccountRepository
import party.morino.kerria.paper.event.KerriaTransactionEvent
import java.math.BigDecimal
import java.util.UUID

/**
 * 経済操作のビジネスロジックを実装するクラス
 *
 * アトミックなDB操作で入金・出金・送金を行い、ログ記録の結果も確認する。
 * ログ失敗時はトランザクション全体をロールバックする。
 */
class EconomyManagerImpl : EconomyManager, KoinComponent {
    private val accountRepository: AccountRepository by inject()
    private val logManager: LogManager by inject()
    private val currencyManager: CurrencyManager by inject()

    override fun deposit(
        accountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String?,
        treatePluginName: String?,
    ): Either<KerriaError, BigDecimal> {
        // 金額バリデーション
        if (amount <= BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(amount, "Amount must be positive").left()
        }
        // 通貨が存在するか確認
        currencyManager.getCurrency(currencyId).onLeft { return it.left() }

        // イベント発火（キャンセル可能）
        val event = KerriaTransactionEvent(
            KerriaTransactionEvent.TransactionType.DEPOSIT,
            accountId, accountId, currencyId, amount, treatePluginName,
        )
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return KerriaError.InvalidAmount(amount, "Transaction cancelled by event").left()
        }

        return runCatching {
            transaction {
                // アカウントの存在確認
                accountRepository.findById(accountId)
                    ?: throw KerriaError.AccountNotFound(accountId.toString())

                // 残高行を確保し、アトミックに加算
                accountRepository.ensureBalanceRow(accountId, currencyId)
                accountRepository.addBalance(accountId, currencyId, amount)

                // 取引ログを記録（失敗時は throw してロールバック）
                logManager.logTransaction(accountId, accountId, currencyId, amount, message, treatePluginName)
                    .onLeft { throw it }

                // 更新後の残高を返す
                accountRepository.getBalance(accountId, currencyId).right()
            }
        }.getOrElse { e ->
            when (e) {
                is KerriaError -> e.left()
                else -> KerriaError.DatabaseError("Deposit failed: ${e.message}", e).left()
            }
        }
    }

    override fun withdraw(
        accountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String?,
        treatePluginName: String?,
    ): Either<KerriaError, BigDecimal> {
        // 金額バリデーション
        if (amount <= BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(amount, "Amount must be positive").left()
        }
        // 通貨が存在するか確認
        currencyManager.getCurrency(currencyId).onLeft { return it.left() }

        // イベント発火（キャンセル可能）
        val event = KerriaTransactionEvent(
            KerriaTransactionEvent.TransactionType.WITHDRAW,
            accountId, accountId, currencyId, amount, treatePluginName,
        )
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return KerriaError.InvalidAmount(amount, "Transaction cancelled by event").left()
        }

        return runCatching {
            transaction {
                // アカウントの存在確認
                accountRepository.findById(accountId)
                    ?: throw KerriaError.AccountNotFound(accountId.toString())

                // アトミックに減算（残高チェック付き）
                val rows = accountRepository.subtractBalance(accountId, currencyId, amount)
                if (rows == 0) {
                    val currentBalance = accountRepository.getBalance(accountId, currencyId)
                    throw KerriaError.InsufficientBalance(
                        required = amount,
                        actual = currentBalance,
                    )
                }

                // 取引ログを記録（失敗時は throw してロールバック）
                logManager.logTransaction(accountId, accountId, currencyId, amount.negate(), message, treatePluginName)
                    .onLeft { throw it }

                // 更新後の残高を返す
                accountRepository.getBalance(accountId, currencyId).right()
            }
        }.getOrElse { e ->
            when (e) {
                is KerriaError -> e.left()
                else -> KerriaError.DatabaseError("Withdraw failed: ${e.message}", e).left()
            }
        }
    }

    override fun setBalance(
        accountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String?,
        treatePluginName: String?,
    ): Either<KerriaError, BigDecimal> {
        // 金額バリデーション
        if (amount < BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(amount, "Amount cannot be negative").left()
        }
        // 通貨が存在するか確認
        currencyManager.getCurrency(currencyId).onLeft { return it.left() }

        // イベント発火（キャンセル可能）
        val event = KerriaTransactionEvent(
            KerriaTransactionEvent.TransactionType.SET_BALANCE,
            accountId, accountId, currencyId, amount, treatePluginName,
        )
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return KerriaError.InvalidAmount(amount, "Transaction cancelled by event").left()
        }

        return runCatching {
            transaction {
                // アカウントの存在確認
                accountRepository.findById(accountId)
                    ?: throw KerriaError.AccountNotFound(accountId.toString())

                // 残高を直接設定
                accountRepository.setBalance(accountId, currencyId, amount)

                // 取引ログを記録（失敗時は throw してロールバック）
                logManager.logTransaction(accountId, accountId, currencyId, amount, message, treatePluginName)
                    .onLeft { throw it }

                // 設定後の残高を返す
                accountRepository.getBalance(accountId, currencyId).right()
            }
        }.getOrElse { e ->
            when (e) {
                is KerriaError -> e.left()
                else -> KerriaError.DatabaseError("Set balance failed: ${e.message}", e).left()
            }
        }
    }

    override fun transfer(
        fromAccountId: UUID,
        toAccountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String?,
        treatePluginName: String?,
    ): Either<KerriaError, Unit> {
        // 自分自身への送金チェック
        if (fromAccountId == toAccountId) {
            return KerriaError.TransferToSelf().left()
        }
        // 金額バリデーション
        if (amount <= BigDecimal.ZERO) {
            return KerriaError.InvalidAmount(amount, "Amount must be positive").left()
        }
        // 通貨が存在するか確認
        currencyManager.getCurrency(currencyId).onLeft { return it.left() }

        // イベント発火（キャンセル可能）
        val event = KerriaTransactionEvent(
            KerriaTransactionEvent.TransactionType.TRANSFER,
            fromAccountId, toAccountId, currencyId, amount, treatePluginName,
        )
        Bukkit.getPluginManager().callEvent(event)
        if (event.isCancelled) {
            return KerriaError.InvalidAmount(amount, "Transaction cancelled by event").left()
        }

        return runCatching {
            transaction {
                // 送金元の存在確認
                accountRepository.findById(fromAccountId)
                    ?: throw KerriaError.AccountNotFound(fromAccountId.toString())
                // 送金先の存在確認
                accountRepository.findById(toAccountId)
                    ?: throw KerriaError.AccountNotFound(toAccountId.toString())

                // 送金元からアトミックに減算（残高チェック付き）
                val rows = accountRepository.subtractBalance(fromAccountId, currencyId, amount)
                if (rows == 0) {
                    val currentBalance = accountRepository.getBalance(fromAccountId, currencyId)
                    throw KerriaError.InsufficientBalance(
                        required = amount,
                        actual = currentBalance,
                    )
                }

                // 送金先に加算
                accountRepository.ensureBalanceRow(toAccountId, currencyId)
                accountRepository.addBalance(toAccountId, currencyId, amount)

                // 取引ログを記録（失敗時は throw してロールバック）
                logManager.logTransaction(fromAccountId, toAccountId, currencyId, amount, message, treatePluginName)
                    .onLeft { throw it }

                Unit.right()
            }
        }.getOrElse { e ->
            when (e) {
                is KerriaError -> e.left()
                else -> KerriaError.DatabaseError("Transfer failed: ${e.message}", e).left()
            }
        }
    }
}
