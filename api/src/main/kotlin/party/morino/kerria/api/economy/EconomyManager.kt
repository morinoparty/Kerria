package party.morino.kerria.api.economy

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal
import java.util.UUID

/**
 * 経済操作の中心となるインターフェース
 *
 * 入金・出金・送金のビジネスロジックを提供します。
 * バリデーション、残高チェック、取引ログ記録を一元管理します。
 */
interface EconomyManager {

    /**
     * 指定アカウントに入金する
     *
     * @param accountId 入金先アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 入金額
     * @param message 取引メモ（任意）
     * @param treatePluginName 取引を実行したプラグイン名（任意）
     * @return 入金後の残高、もしくはエラー
     */
    fun deposit(
        accountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String? = null,
        treatePluginName: String? = null,
    ): Either<KerriaError, BigDecimal>

    /**
     * 指定アカウントから出金する
     *
     * @param accountId 出金元アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 出金額
     * @param message 取引メモ（任意）
     * @param treatePluginName 取引を実行したプラグイン名（任意）
     * @return 出金後の残高、もしくはエラー
     */
    fun withdraw(
        accountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String? = null,
        treatePluginName: String? = null,
    ): Either<KerriaError, BigDecimal>

    /**
     * アカウント間で送金する
     *
     * @param fromAccountId 送金元アカウントのUUID
     * @param toAccountId 送金先アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 送金額
     * @param message 取引メモ（任意）
     * @param treatePluginName 取引を実行したプラグイン名（任意）
     * @return 成功時はUnit、失敗時はエラー
     */
    fun transfer(
        fromAccountId: UUID,
        toAccountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String? = null,
        treatePluginName: String? = null,
    ): Either<KerriaError, Unit>

    /**
     * 指定アカウントの残高を直接設定する（管理者用）
     *
     * @param accountId 対象アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 設定する残高
     * @param message 取引メモ（任意）
     * @param treatePluginName 取引を実行したプラグイン名（任意）
     * @return 設定後の残高、もしくはエラー
     */
    fun setBalance(
        accountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String? = null,
        treatePluginName: String? = null,
    ): Either<KerriaError, BigDecimal>
}
