package party.morino.kerria.api.account

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal
import java.util.UUID

/**
 * アカウント管理機能を提供するインターフェース
 *
 * プレイヤーおよびサービスのアカウント情報を管理し、
 * 残高の取得機能を提供します。
 * すべての操作は[Either]を返し、エラーハンドリングを型安全に行います。
 */
interface AccountManager {

    /**
     * プレイヤーのUUIDからアカウントを取得します
     *
     * @param playerUniqueId プレイヤーのUUID
     * @return アカウントの取得結果。成功時は[Account]、失敗時は[KerriaError]を返します
     */
    fun getAccount(playerUniqueId: UUID): Either<KerriaError, Account>

    /**
     * プレイヤーのUUIDからアカウントを取得し、存在しない場合は作成します
     *
     * @param playerUniqueId プレイヤーのUUID
     * @param playerName プレイヤー名（新規作成時に使用）
     * @return アカウントの取得または作成結果
     */
    fun getOrCreateAccount(playerUniqueId: UUID, playerName: String): Either<KerriaError, Account>

    /**
     * サービス名からサービスアカウントを取得し、存在しない場合は作成します
     *
     * プラグインやシステムが通貨の発行・回収を行う際に使用するアカウントです。
     *
     * @param serviceName サービス名（例: "shop_plugin", "lottery"）
     * @param accountType アカウント種別（SERVER, PLUGIN, SYSTEMのいずれか）
     * @return サービスアカウントの取得または作成結果
     */
    fun getOrCreateServiceAccount(
        serviceName: String,
        accountType: AccountType = AccountType.PLUGIN,
    ): Either<KerriaError, Account>

    /**
     * アカウントIDと通貨IDから残高を取得します
     *
     * @param accountId アカウントのUUID
     * @param currencyId 通貨のID
     * @return 残高の取得結果
     */
    fun getBalance(accountId: UUID, currencyId: Int): Either<KerriaError, BigDecimal>
}
