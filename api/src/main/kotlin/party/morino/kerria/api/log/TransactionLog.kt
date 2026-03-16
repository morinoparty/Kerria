package party.morino.kerria.api.log

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * 取引ログを表現する不変データクラス
 *
 * @property id 取引ログの一意ID
 * @property fromAccountId 送金元アカウントのUUID
 * @property toAccountId 送金先アカウントのUUID
 * @property currencyId 通貨のID
 * @property amount 取引金額
 * @property message 取引に関するメモ・説明
 * @property timestamp 取引時刻
 */
data class TransactionLog(
    val id: Long,
    val fromAccountId: UUID,
    val toAccountId: UUID,
    val currencyId: Int,
    val amount: BigDecimal,
    val message: String?,
    val timestamp: LocalDateTime,
)
