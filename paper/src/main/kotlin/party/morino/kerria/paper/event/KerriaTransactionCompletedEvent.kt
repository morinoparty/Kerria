package party.morino.kerria.paper.event

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal
import java.util.UUID

/**
 * 経済操作（入金・出金・送金・残高設定）がデータベースにコミットされた後に発火するイベント
 *
 * [KerriaTransactionEvent] が操作の実行「前」に発火するキャンセル可能なイベントであるのに対し、
 * このイベントは操作が「成功して確定した後」に発火する。既に確定した事実を通知するため
 * キャンセルはできない。ロールバックされた（失敗した）操作では発火しない。
 *
 * スコアボードの更新・通知・外部システムへの同期など、確定した残高変更を観測する用途に用いる。
 *
 * @property type 取引の種類
 * @property fromAccountId 送金元アカウントID
 * @property toAccountId 送金先アカウントID
 * @property currencyId 通貨ID
 * @property amount 取引金額
 * @property callerPluginName 操作を実行したプラグイン名
 * @property fromBalance 送金元アカウントの確定後残高
 * @property toBalance 送金先アカウントの確定後残高
 */
class KerriaTransactionCompletedEvent(
    val type: KerriaTransactionEvent.TransactionType,
    val fromAccountId: UUID,
    val toAccountId: UUID,
    val currencyId: Int,
    val amount: BigDecimal,
    val callerPluginName: String?,
    val fromBalance: BigDecimal,
    val toBalance: BigDecimal,
) : Event(!Bukkit.isPrimaryThread()) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
