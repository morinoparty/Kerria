package party.morino.kerria.addons.quickshop.message

/**
 * ショップを利用したプレイヤーから見た取引の種類
 */
enum class ShopTradeType {
    // 販売ショップ（QuickShop の SELLING）からアイテムを購入した
    PURCHASE,

    // 買取ショップ（QuickShop の BUYING）へアイテムを売却した
    SALE,
}
