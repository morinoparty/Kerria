package party.morino.kerria.account

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.sql.transactions.transaction
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.error.PlayerError
import java.util.UUID

/**
 * アカウント管理機能の実装クラス
 *
 * このクラスは[AccountManager]インターフェースを実装し、プレイヤーのアカウント情報の管理を担当します。
 * データベースとの通信を行い、アカウント情報の取得や更新を行います。
 * すべての操作はトランザクション内で実行され、エラーが発生した場合は適切なエラー型を返します。
 *
 * @see AccountManager アカウント管理のインターフェース定義
 * @see Account アカウント情報を表すインターフェース
 * @see KerriaError エラー型の基底インターフェース
 */
class AccountManagerImpl() : AccountManager {

    /**
     * プレイヤーのUUIDからアカウントを取得します
     *
     * このメソッドは指定されたUUIDに対応するプレイヤーのアカウント情報をデータベースから取得します。
     * トランザクション内で実行され、プレイヤーが見つからない場合はエラーを返します。
     *
     * @param player 取得対象のプレイヤー
     * @return Either<KerriaError, Account> 成功時はアカウント情報、失敗時はエラー情報を含むEither
     * @throws PlayerError.PlayerNotFound 指定されたUUIDのプレイヤーが見つからない場合
     */
    override fun getAccount(player: OfflinePlayer?): Either<KerriaError, Account> = transaction {
        AccountImpl.findByOfflinePlayer(player)?.right()
            ?: PlayerError.PlayerNotFound(player.uniqueId.toString()).left()
    }


    /**
     * アカウントIDからアカウントを取得します
     *
     * このメソッドは指定されたアカウントIDに対応するアカウント情報をデータベースから取得します。
     * トランザクション内で実行され、アカウントが見つからない場合はエラーを返します。
     *
     * @param accountUniqueId 取得対象のアカウントID
     * @return Either<KerriaError, Account> 成功時はアカウント情報、失敗時はエラー情報を含むEither
     * @throws PlayerError.PlayerNotFound 指定されたIDのアカウントが見つからない場合
     */
    override fun getAccount(accountUniqueId: UUID): Either<KerriaError, Account> = transaction {
        AccountImpl.findByAccountUniqueId(accountUniqueId)?.right()
            ?: PlayerError.PlayerNotFound(accountUniqueId.toString()).left()
    }
} 