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
}
