package party.morino.kerria.api.account

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal
import java.util.UUID

/**
 * 共有の銀行アカウントを管理するインターフェース
 *
 * ギルドや町の金庫のような、複数プレイヤーで共有する残高を扱う。
 * Vault の Bank API のバックエンドとして利用される。
 * すべての操作は [Either] を返し、型安全にエラーを扱う。
 */
interface BankManager {

    /**
     * 銀行を作成する
     *
     * @param name 銀行名（一意）
     * @param ownerUuid 所有者プレイヤーのUUID
     * @return 作成された銀行、もしくはエラー（同名が存在する場合は [KerriaError.BankAlreadyExists]）
     */
    fun createBank(name: String, ownerUuid: UUID): Either<KerriaError, Bank>

    /**
     * 銀行を削除する
     *
     * @param name 銀行名
     * @return 成功時はUnit、失敗時はエラー
     */
    fun deleteBank(name: String): Either<KerriaError, Unit>

    /**
     * 銀行名から銀行を取得する
     *
     * @param name 銀行名
     * @return 銀行、もしくはエラー（見つからない場合は [KerriaError.BankNotFound]）
     */
    fun getBank(name: String): Either<KerriaError, Bank>

    /**
     * すべての銀行を取得する
     *
     * @return 銀行のリスト、もしくはエラー
     */
    fun listBanks(): Either<KerriaError, List<Bank>>

    /**
     * 銀行の残高を取得する
     *
     * @param name 銀行名
     * @param currencyId 通貨のID
     * @return 残高、もしくはエラー
     */
    fun bankBalance(name: String, currencyId: Int): Either<KerriaError, BigDecimal>

    /**
     * 銀行に入金する
     *
     * @param name 銀行名
     * @param currencyId 通貨のID
     * @param amount 入金額
     * @param treatePluginName 操作を実行したプラグイン名（任意）
     * @return 入金後の残高、もしくはエラー
     */
    fun deposit(
        name: String,
        currencyId: Int,
        amount: BigDecimal,
        treatePluginName: String? = null,
    ): Either<KerriaError, BigDecimal>

    /**
     * 銀行から出金する
     *
     * @param name 銀行名
     * @param currencyId 通貨のID
     * @param amount 出金額
     * @param treatePluginName 操作を実行したプラグイン名（任意）
     * @return 出金後の残高、もしくはエラー
     */
    fun withdraw(
        name: String,
        currencyId: Int,
        amount: BigDecimal,
        treatePluginName: String? = null,
    ): Either<KerriaError, BigDecimal>

    /**
     * 指定プレイヤーが銀行の所有者かどうかを返す
     *
     * @param name 銀行名
     * @param uuid プレイヤーのUUID
     * @return 所有者なら true、もしくはエラー
     */
    fun isOwner(name: String, uuid: UUID): Either<KerriaError, Boolean>

    /**
     * 指定プレイヤーが銀行のメンバー（所有者を含む）かどうかを返す
     *
     * @param name 銀行名
     * @param uuid プレイヤーのUUID
     * @return メンバーなら true、もしくはエラー
     */
    fun isMember(name: String, uuid: UUID): Either<KerriaError, Boolean>

    /**
     * 銀行にメンバーを追加する
     *
     * @param name 銀行名
     * @param uuid プレイヤーのUUID
     * @return 成功時はUnit、失敗時はエラー
     */
    fun addMember(name: String, uuid: UUID): Either<KerriaError, Unit>

    /**
     * 銀行からメンバーを削除する（所有者は削除できない）
     *
     * @param name 銀行名
     * @param uuid プレイヤーのUUID
     * @return 成功時はUnit、失敗時はエラー
     */
    fun removeMember(name: String, uuid: UUID): Either<KerriaError, Unit>
}
