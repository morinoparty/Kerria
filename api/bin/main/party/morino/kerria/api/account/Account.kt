package party.morino.kerria.api.account

import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.log.Log
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * プレイヤーのアカウント情報を表現するインターフェース
 * 
 * アカウントの基本情報（ID、UUID、プレイヤー名）、残高管理、
 * および取引履歴の取得機能を提供します。
 */
interface Account {
    /**
     * アカウントのIDを取得します
     * 
     * @return アカウントの一意のID (uuid v7)
     */
    fun getAccountUniqueId(): UUID

    /**
     * プレイヤーのUUIDを取得します
     * 
     * @return プレイヤーの一意のUUID (uuid v4)
     */
    fun getPlayerUniqueId(): UUID

    /**
     * プレイヤー名を取得します
     * 
     * @return プレイヤーの名前
     */
    fun getPlayerName(): String

    /**
     * アカウントの現在の残高を取得します
     * 
     * @return 現在の残高
     */
    suspend fun getBalance(): BigDecimal


    /**
     * アカウントの現在の残高を取得します
     *
     * @return 現在の残高
     */
    suspend fun getBalance(currency: Currency): BigDecimal

    /**
     * アカウントの残高を設定します
     * 
     * @param balance 設定する残高
     */
    suspend fun setBalance(balance: BigDecimal)

    /**
     * アカウントの残高を設定します
     * 
     * @param balance 設定する残高
     */
    suspend fun setBalance(balance: BigDecimal, currency: Currency)
    
    /**
     * アカウントの取引履歴を取得します
     * 
     * @param since この日時以降の取引履歴を取得
     * @param until この日時以前の取引履歴を取得
     * @param limit 取得する履歴の最大件数
     * @param offset 取得開始位置
     * @return 取引履歴のリスト
     */
    suspend fun getLogs(since: LocalDateTime, until: LocalDateTime, limit: Int, offset: Int): List<Log>
}