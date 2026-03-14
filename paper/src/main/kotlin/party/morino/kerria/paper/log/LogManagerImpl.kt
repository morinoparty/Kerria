package party.morino.kerria.paper.log

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.Dispatchers
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.error.PlayerError
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.api.log.TransactionLog
import party.morino.kerria.paper.account.AccountImpl
import java.time.LocalDateTime

/**
 * 取引ログ管理機能の実装クラス
 *
 * このクラスは[LogManager]インターフェースを実装し、プレイヤー間の取引履歴の管理を担当します。
 * データベースを使用して取引ログの記録と取得を行います。
 * すべての操作はトランザクション内で実行され、エラーが発生した場合は適切なエラー型を返します。
 *
 * @property plugin プラグインのインスタンス
 * @see LogManager 取引ログ管理のインターフェース定義
 * @see TransactionLog 取引ログを表すインターフェース
 */
class LogManagerImpl(private val plugin: JavaPlugin) : LogManager {

    /**
     * 取引ログを記録します
     *
     * @param fromAccount 送金元プレイヤーのアカウント
     * @param toAccount 送金先プレイヤーのアカウント
     * @param amount 取引金額
     * @param timestamp 取引時刻
     * @return Either<KerriaError, Unit> 成功時はUnit、失敗時はエラー情報を含むEither
     */
    override fun logTransaction(
        fromAccount: Account,
        toAccount: Account,
        amount: Long,
        timestamp: LocalDateTime,
    ): Either<KerriaError, Unit> = transaction {
        val fromAccount = AccountImpl.findByAccountUniqueId(fromAccount.getAccountUniqueId())
            ?: return@transaction PlayerError.PlayerNotFound(fromAccount.getAccountUniqueId().toString()).left()
        val toAccount = AccountImpl.findByAccountUniqueId(toAccount.getAccountUniqueId())
            ?: return@transaction PlayerError.PlayerNotFound(toAccount.getAccountUniqueId().toString()).left()

        Unit.right()
    }

    /**
     * プレイヤーの取引履歴を取得します
     *
     * @param account プレイヤーのアカウント
     * @param limit 取得する履歴の最大件数
     * @param offset 取得開始位置
     * @return Either<KerriaError, List<TransactionLog>> 成功時は取引履歴のリスト、失敗時はエラー情報を含むEither
     */
    override suspend fun getTransactionHistory(
        account: Account,
        limit: Int,
        offset: Int,
    ): Either<KerriaError, List<TransactionLog>> = transaction {
        val account = AccountImpl.findByAccountUniqueId(account.getAccountUniqueId())
            ?: return@transaction PlayerError.PlayerNotFound(account.getAccountUniqueId().toString()).left()

        TODO()
    }
}
