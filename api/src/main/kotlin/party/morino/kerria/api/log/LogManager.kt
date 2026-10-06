package party.morino.kerria.api.log

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * 取引ログ管理機能を提供するインターフェース
 *
 * 取引の記録と履歴取得を行います。
 */
interface LogManager {

    /**
     * 取引ログを記録します
     *
     * @param fromAccountId 送金元アカウントのUUID
     * @param toAccountId 送金先アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 取引金額
     * @param message 取引メモ（任意）
     * @param treatePluginName 取引を実行したプラグイン名（任意）
     * @param timestamp 取引時刻
     * @return 成功時はUnit、失敗時はエラー
     */
    fun logTransaction(
        fromAccountId: UUID,
        toAccountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String? = null,
        treatePluginName: String? = null,
        timestamp: LocalDateTime = LocalDateTime.now(),
    ): Either<KerriaError, Unit>

    /**
     * アカウントの取引履歴を取得します
     *
     * @param accountId アカウントのUUID
     * @param limit 取得する最大件数
     * @param offset 取得開始位置
     * @return 取引履歴のリスト、もしくはエラー
     */
    fun getTransactionHistory(
        accountId: UUID,
        limit: Int = 10,
        offset: Int = 0,
    ): Either<KerriaError, List<TransactionLog>>

    /**
     * 指定日時より前の取引ログの件数を取得します
     *
     * @param cutoff 基準日時（この日時より前のログが対象）
     * @return 対象の件数、もしくはエラー
     */
    fun countLogsOlderThan(cutoff: LocalDateTime): Either<KerriaError, Long>

    /**
     * 指定日時より前の取引ログを削除します
     *
     * データベースの肥大化を防ぐため、古い取引ログを整理する用途を想定しています。
     *
     * @param cutoff 基準日時（この日時より前のログを削除）
     * @return 削除した件数、もしくはエラー
     */
    fun deleteLogsOlderThan(cutoff: LocalDateTime): Either<KerriaError, Int>
}
