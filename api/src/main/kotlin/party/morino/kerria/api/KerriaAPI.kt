package party.morino.kerria.api

import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.log.LogManager

/**
 * KerriaプラグインのメインAPIインターフェース
 * 
 * このインターフェースを通じて、Kerriaの主要な機能にアクセスできます。
 * アカウント管理、ログ管理、設定管理などの機能を提供します。
 */
interface KerriaAPI {

    /**
     * アカウント管理機能へのアクセスを提供します
     * 
     * @return アカウント管理機能を持つ[party.morino.kerria.api.account.AccountManager]のインスタンス
     */
    fun getAccountManager(): AccountManager

    /**
     * ログ管理機能へのアクセスを提供します
     * 
     * @return ログ管理機能を持つ[party.morino.kerria.api.log.LogManager]のインスタンス
     */
    fun getLogManager(): LogManager

    /**
     * 設定管理機能へのアクセスを提供します
     * 
     * @return 設定管理機能を持つ[party.morino.kerria.api.files.ConfigManager]のインスタンス
     */
    fun getConfigManager(): ConfigManager
}