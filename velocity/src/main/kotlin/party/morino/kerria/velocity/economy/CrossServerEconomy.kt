package party.morino.kerria.velocity.economy

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.common.database.repository.AccountRepository
import party.morino.kerria.common.database.repository.CurrencyRepository
import java.math.BigDecimal
import java.util.UUID

/**
 * 共有データベースを直接参照して、クロスサーバーの残高照会・送金を行う
 *
 * Paper 側と同じテーブル・リポジトリ（[common] モジュール）を利用するため、
 * PostgreSQL を共有していれば、どのサーバーからでも同じ残高を参照・更新できる。
 * Bukkit のイベントには依存しないため、Velocity 上でも動作する。
 */
class CrossServerEconomy(private val defaultCurrencyId: Int) {
    private val accountRepository = AccountRepository()
    private val currencyRepository = CurrencyRepository()

    /** 送金結果 */
    sealed interface TransferResult {
        data object Success : TransferResult
        data object AccountNotFound : TransferResult
        data object InsufficientFunds : TransferResult
        data class Error(val message: String) : TransferResult
    }

    /** 通貨情報を取得する */
    fun currency(currencyId: Int = defaultCurrencyId): Currency? =
        transaction { currencyRepository.findById(currencyId) }

    /** プレイヤーUUIDから残高を取得する（アカウントが無ければ null） */
    fun balanceOf(uuid: UUID, currencyId: Int = defaultCurrencyId): BigDecimal? =
        transaction {
            val account = accountRepository.findByPlayerUniqueId(uuid) ?: return@transaction null
            accountRepository.getBalance(account.accountId, currencyId)
        }

    /** プレイヤー名からアカウントを解決する（大文字小文字を区別しない） */
    fun resolveAccountByName(name: String): Account? =
        transaction { accountRepository.findPlayerByName(name) }

    /**
     * プレイヤー間で送金する（アトミック）
     *
     * 送金元の残高から減算し、送金先へ加算する。減算・加算を単一トランザクションで実行する。
     */
    fun transfer(
        fromUuid: UUID,
        toUuid: UUID,
        amount: BigDecimal,
        currencyId: Int = defaultCurrencyId,
    ): TransferResult =
        try {
            transaction {
                val from = accountRepository.findByPlayerUniqueId(fromUuid)
                    ?: return@transaction TransferResult.AccountNotFound
                val to = accountRepository.findByPlayerUniqueId(toUuid)
                    ?: return@transaction TransferResult.AccountNotFound

                // 残高チェック付きの減算（0行なら残高不足）
                val rows = accountRepository.subtractBalance(from.accountId, currencyId, amount)
                if (rows == 0) {
                    return@transaction TransferResult.InsufficientFunds
                }
                accountRepository.ensureBalanceRow(to.accountId, currencyId)
                accountRepository.addBalance(to.accountId, currencyId, amount)
                TransferResult.Success
            }
        } catch (e: Exception) {
            TransferResult.Error(e.message ?: "Unknown error")
        }
}
