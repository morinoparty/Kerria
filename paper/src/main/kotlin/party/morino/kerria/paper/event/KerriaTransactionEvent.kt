package party.morino.kerria.paper.event

import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal
import java.util.UUID

/**
 * 経済操作（入金・出金・送金）が実行される前に発火するイベント
 *
 * キャンセル可能。キャンセルされた場合、操作は実行されない。
 *
 * @property type 取引の種類
 * @property fromAccountId 送金元アカウントID
 * @property toAccountId 送金先アカウントID
 * @property currencyId 通貨ID
 * @property amount 取引金額
 * @property callerPluginName 操作を実行したプラグイン名
 */
class KerriaTransactionEvent(
    val type: TransactionType,
    val fromAccountId: UUID,
    val toAccountId: UUID,
    val currencyId: Int,
    val amount: BigDecimal,
    val callerPluginName: String?,
) : Event(!Bukkit.isPrimaryThread()), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    /**
     * 取引の種類
     */
    enum class TransactionType {
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        SET_BALANCE,
    }
}
