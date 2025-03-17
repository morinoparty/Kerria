package party.morino.kerria.api.log

import arrow.core.Either
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.error.KerriaError
import java.time.LocalDateTime
import java.util.UUID

/**
 * 取引ログ管理機能を提供するインターフェース
 */
interface LogManager {
    /**
     * 取引ログを記録します
     * 
     * @param fromAccount 送金元プレイヤーのアカウント
     * @param toAccount 送金先プレイヤーのアカウント
     * @param amount 取引金額
     * @param timestamp 取引時刻
     * @return ログ記録の結果。成功時はUnit、失敗時はエラー情報を返します
     */
    fun logTransaction(
        fromAccount: Account,
        toAccount: Account,
        amount: Long,
        timestamp: LocalDateTime = LocalDateTime.now()
    ): Either<KerriaError, Unit>

    /**
     * プレイヤーの取引履歴を取得します
     * 
     * @param account プレイヤーのアカウント
     * @param limit 取得する履歴の最大件数
     * @param offset 取得開始位置
     * @return 取引履歴の取得結果。成功時は[TransactionLog]のリスト、失敗時はエラー情報を返します
     */
    suspend fun getTransactionHistory(
        account: Account,
        limit: Int = 10,
        offset: Int = 0
    ): Either<KerriaError, List<TransactionLog>>
}


data class TransactionLog(
    val id: Long,
    val fromAccount: Account,
    val toAccount: Account,
    val amount: Long,
    val timestamp: LocalDateTime
) 