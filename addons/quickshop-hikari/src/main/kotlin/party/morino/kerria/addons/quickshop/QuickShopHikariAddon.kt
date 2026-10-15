package party.morino.kerria.addons.quickshop

import org.bukkit.plugin.java.JavaPlugin
import party.morino.kerria.addons.quickshop.listener.ShopTradeListener

/**
 * QuickShop-Hikari 連携アドオン
 *
 * QuickShop のショップでの購入・売却の内容（誰が・誰のショップで・何を・何個）を、
 * Kerria の取引ログのメッセージとして記録する。
 * Kerria と QuickShop-Hikari は paper-plugin.yml の dependencies で必須にしているため、ここでは存在確認をしない。
 */
class QuickShopHikariAddon : JavaPlugin() {

    override fun onEnable() {
        // QuickShop の取引イベントを購読する
        server.pluginManager.registerEvents(ShopTradeListener(this), this)
        logger.info("Kerria QuickShop-Hikari addon has been enabled.")
    }
}
