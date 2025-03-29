package party.morino.kerria.api.account

import arrow.core.Either
import org.bukkit.OfflinePlayer
import party.morino.kerria.api.error.KerriaError
import java.util.UUID

/**
 * アカウント管理機能を提供するインターフェース
 *
 * プレイヤーのアカウント情報を管理し、UUIDやIDを使用してアカウントにアクセスする機能を提供します。
 * すべての操作は[Either]を返し、エラーハンドリングを型安全に行います。
 */
interface AccountManager {

    /**
     * プレイヤーのUUIDからアカウントを取得します
     *
     * @param player 取得対象のプレイヤー
     * @return アカウントの取得結果。成功時は[Account]、失敗時は[KerriaError]を返します
     * @throws PlayerNotFound 指定されたUUIDのプレイヤーが見つからない場合
     * @throws DatabaseError データベース操作に失敗した場合
     */
    fun getAccount(player: OfflinePlayer): Either<KerriaError, Account>

    /**
     * アカウントIDからアカウントを取得します
     *
     * @param uniqueId 取得対象のアカウントID(uuid v7)
     * @return アカウントの取得結果。成功時は[Account]、失敗時は[KerriaError]を返します
     * @throws PlayerNotFound 指定されたIDのアカウントが見つからない場合
     * @throws DatabaseError データベース操作に失敗した場合
     */
    fun getAccount(uniqueId: UUID): Either<KerriaError, Account>
}