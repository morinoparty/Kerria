package party.morino.kerria.log

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.Dispatchers
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import party.morino.kerria.account.AccountImpl
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.error.PlayerError
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.api.log.TransactionLog
import party.morino.kerria.model.TransactionLogTable
import java.time.LocalDateTime
import java.util.UUID

/**
 * 取引ログ管理機能の実装クラス
 *
 * このクラスは[LogManager]インターフェースを実装し、プレイヤー間の取引履歴の管理を担当します。
 * SQLiteデータベースを使用して取引ログの記録と取得を行います。
 * すべての操作はトランザクション内で実行され、エラーが発生した場合は適切なエラー型を返します。
 *
 * @property plugin プラグインのインスタンス
 * @see LogManager 取引ログ管理のインターフェース定義
 * @see TransactionLog 取引ログを表すインターフェース
 * @see TransactionLogTable 取引ログのテーブル定義
 */
class LogManagerImpl(private val plugin: JavaPlugin) : LogManager {


    /**
     * 取引ログを記録します
     *
     * このメソッドは2つのプレイヤー間の取引情報をデータベースに記録します。
     * 送金元と送金先のプレイヤーが存在することを確認し、取引情報を保存します。
     *
     * @param fromAccount 送金元プレイヤーのアカウント
     * @param toAccount 送金先プレイヤーのアカウント
     * @param amount 取引金額
     * @param timestamp 取引時刻
     * @return Either<KerriaError, Unit> 成功時はUnit、失敗時はエラー情報を含むEither
     * @throws PlayerError.PlayerNotFound 指定されたUUIDのプレイヤーが見つからない場合
     */
    override fun logTransaction(
        fromAccount: Account,
        toAccount: Account,
        amount: Long,
        timestamp: LocalDateTime
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
     * このメソッドは指定されたプレイヤーの取引履歴をデータベースから取得します。
     * 取引履歴は新しい順に取得され、ページネーションに対応しています。
     *
     * @param offlinePlayer プレイヤー
     * @param limit 取得する履歴の最大件数
     * @param offset 取得開始位置
     * @return Either<KerriaError, List<TransactionLog>> 成功時は取引履歴のリスト、失敗時はエラー情報を含むEither
     * @throws PlayerError.PlayerNotFound 指定されたUUIDのプレイヤーが見つからない場合
     */
    override suspend fun getTransactionHistory(
        account: Account,
        limit: Int,
        offset: Int
    ): Either<KerriaError, List<TransactionLog>> = newSuspendedTransaction(Dispatchers.IO) {
        val account = AccountImpl.findByAccountUniqueId(account.getAccountUniqueId())
            ?: return@newSuspendedTransaction PlayerError.PlayerNotFound(account.getAccountUniqueId().toString()).left()

        TODO()
    }
} 