package party.morino.kerria.addons.quickshop.message

/**
 * QuickShop の取引内容から、Kerria の取引ログに記録するメッセージを組み立てる
 *
 * 1 回の取引で、購入者の引き落とし・オーナーへの入金・税金の支払いがそれぞれ別の取引ログになるため、
 * どのログから見ても誰と誰の何の取引か分かるよう、両者の名前を含める。
 */
object ShopTradeMessageFactory {
    // メッセージの先頭に付けて、QuickShop の取引であることを示す
    private const val PREFIX = "QuickShop: "

    /**
     * 取引ログに記録するメッセージを組み立てる
     *
     * @param trade 取引の内容
     * @return 取引ログに記録するメッセージ（プレーンテキスト）
     */
    fun create(trade: ShopTrade): String = when (trade.type) {
        ShopTradeType.PURCHASE ->
            "$PREFIX${trade.purchaserName} が ${trade.ownerName} のショップで ${trade.itemName} ×${trade.amount} を購入"
        ShopTradeType.SALE ->
            "$PREFIX${trade.purchaserName} が ${trade.ownerName} のショップに ${trade.itemName} ×${trade.amount} を売却"
    }
}
