package party.morino.kerria.addons.quickshop.message

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * QuickShop の取引メッセージの組み立てのテスト
 */
class ShopTradeMessageFactoryTest {

    @Test
    @DisplayName("Purchase from a selling shop names both players and the item")
    fun purchaseMessage() {
        val trade = ShopTrade("Steve", "Notch", "diamond", 16, ShopTradeType.PURCHASE)
        assertEquals("QuickShop: Steve が Notch のショップで diamond ×16 を購入", ShopTradeMessageFactory.create(trade))
    }

    @Test
    @DisplayName("Sale to a buying shop uses the sale wording")
    fun saleMessage() {
        val trade = ShopTrade("Steve", "Notch", "diamond", 16, ShopTradeType.SALE)
        assertEquals("QuickShop: Steve が Notch のショップに diamond ×16 を売却", ShopTradeMessageFactory.create(trade))
    }
}
