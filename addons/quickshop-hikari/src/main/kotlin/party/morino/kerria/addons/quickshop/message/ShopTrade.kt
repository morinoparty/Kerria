package party.morino.kerria.addons.quickshop.message

/**
 * QuickShop のショップで行われた 1 回の取引の内容
 *
 * QuickShop の型に依存させず、取引メッセージの組み立てを単体でテストできるようにするためのデータクラス。
 *
 * @property purchaserName ショップを利用したプレイヤーの名前
 * @property ownerName ショップのオーナーの表示名
 * @property itemName 取引されたアイテムの名前
 * @property amount 取引されたアイテムの個数
 * @property type 取引の種類（プレイヤーから見て購入か売却か）
 */
data class ShopTrade(
    val purchaserName: String,
    val ownerName: String,
    val itemName: String,
    val amount: Int,
    val type: ShopTradeType,
)
