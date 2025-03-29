package party.morino.kerria.api.log

import party.morino.kerria.api.account.Account
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 取引ログを表現するデータクラス
 *
 * @property id 取引ID
 * @property senderAccount 送金元プレイヤーのAccount (serializerでid: Longにされる)
 * @property receiverAccount 送金先プレイヤーのAccount (serializerでid: Longにされる)
 * @property amount 取引金額
 * @property timestamp 取引時刻
 * @property message 取引メッセージ 相手にも見える (任意)
 */
data class Log(
    val id: Long,
    val senderAccount: Account,
    val receiverAccount: Account,
    val amount: BigDecimal,
    val timestamp: LocalDateTime,
    val message: String? = null,
)
