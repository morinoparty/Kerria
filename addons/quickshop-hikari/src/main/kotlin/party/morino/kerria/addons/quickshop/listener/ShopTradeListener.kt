package party.morino.kerria.addons.quickshop.listener

import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent
import com.ghostchu.quickshop.api.shop.Shop
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import party.morino.kerria.addons.quickshop.message.ShopTrade
import party.morino.kerria.addons.quickshop.message.ShopTradeMessageFactory
import party.morino.kerria.addons.quickshop.message.ShopTradeType
import party.morino.kerria.api.economy.TransactionContext

/**
 * QuickShop の取引イベントを購読し、取引内容を Kerria の取引ログに付けるリスナー
 *
 * QuickShop-Hikari は 1 回の取引を同じスレッドで次の順に処理する。
 * 1. [ShopPurchaseEvent]（取引前、キャンセル可能）
 * 2. Vault 経由の引き落とし・入金（ここで Kerria が取引ログを記録する）
 * 3. [ShopSuccessPurchaseEvent]（取引成功後）
 *
 * そのため 1 でメッセージを設定し、3 で解除する。
 * 取引が途中で失敗すると 3 が呼ばれないため、次の tick でも必ず解除する。
 *
 * @property plugin 解除処理のスケジュールに使うアドオンのプラグイン
 */
class ShopTradeListener(private val plugin: Plugin) : Listener {

    /**
     * 取引前に、これから行われる Vault 取引のメッセージを設定する
     *
     * 他のプラグインによるキャンセルや金額の変更が確定した後に動くよう、MONITOR で受け取る。
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPurchase(event: ShopPurchaseEvent) {
        val trade = ShopTrade(
            purchaserName = event.purchaser.display,
            ownerName = event.shop.owner.display,
            itemName = itemName(event.shop.item),
            amount = event.amount,
            type = tradeType(event.shop),
        )
        TransactionContext.setMessage(ShopTradeMessageFactory.create(trade))
        // 取引が失敗して成功イベントが来なかった場合でも、後続の取引にメッセージが残らないようにする
        plugin.server.scheduler.runTask(plugin, Runnable { TransactionContext.clear() })
    }

    /**
     * 取引成功後に、設定したメッセージを解除する
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onSuccess(@Suppress("UNUSED_PARAMETER") event: ShopSuccessPurchaseEvent) {
        TransactionContext.clear()
    }

    /**
     * プレイヤーから見た取引の種類を判定する
     *
     * 買取ショップ（isBuying）ではプレイヤーがアイテムを売り、それ以外（販売ショップ）では購入する。
     */
    private fun tradeType(shop: Shop<*, *>): ShopTradeType =
        if (shop.isBuying) ShopTradeType.SALE else ShopTradeType.PURCHASE

    /**
     * アイテムの表示名を取得する（独自の名前があればそれを、無ければアイテムの ID を使う）
     */
    private fun itemName(item: ItemStack): String {
        val customName = item.itemMeta?.customName()
        return if (customName != null) {
            PlainTextComponentSerializer.plainText().serialize(customName)
        } else {
            item.type.key.value()
        }
    }
}
