package party.morino.kerria.api

import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.log.LogManager

/**
 * KerriaプラグインのメインAPIファサード
 *
 * このインターフェースを通じて、Kerriaの主要な機能にアクセスできます。
 * アカウント管理、通貨管理、経済操作、ログ管理の機能を提供します。
 */
interface KerriaAPI {

    /**
     * アカウント管理機能へのアクセスを提供します
     *
     * @return [AccountManager]のインスタンス
     */
    fun getAccountManager(): AccountManager

    /**
     * 通貨管理機能へのアクセスを提供します
     *
     * @return [CurrencyManager]のインスタンス
     */
    fun getCurrencyManager(): CurrencyManager

    /**
     * 経済操作機能へのアクセスを提供します
     *
     * @return [EconomyManager]のインスタンス
     */
    fun getEconomyManager(): EconomyManager

    /**
     * ログ管理機能へのアクセスを提供します
     *
     * @return [LogManager]のインスタンス
     */
    fun getLogManager(): LogManager
}
