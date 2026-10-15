package party.morino.kerria.paper.integration.placeholder

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.common.placeholder.KerriaPlaceholders
import party.morino.kerria.paper.KerriaTest
import java.math.BigDecimal
import kotlin.test.assertEquals

/**
 * MiniPlaceholders のプレースホルダー（<kerria_xxx>）のテスト
 */
@ExtendWith(KerriaTest::class)
class KerriaPlaceholdersTest : KoinTest {

    private val server: ServerMock by inject()
    private val accountManager: AccountManager by inject()
    private val economyManager: EconomyManager by inject()

    // MiniPlaceholders へ登録せず、エクスパンションのリゾルバーを直接使って描画する
    private val expansion = KerriaPlaceholders.create(PaperPlaceholderBalanceSource(), "test")

    /** プレイヤーを対象にプレースホルダーを描画し、プレーンテキストで返す */
    private fun render(input: String, target: net.kyori.adventure.audience.Audience): String {
        val resolvers = net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver(
            expansion.audiencePlaceholders(),
            expansion.globalPlaceholders(),
        )
        return PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(input, target, resolvers))
    }

    @Test
    @DisplayName("Balance placeholders format, strip decimals and accept currency arguments")
    fun balancePlaceholders() {
        val player = server.addPlayer("PlaceholderPlayer")
        val account = accountManager.getOrCreateAccount(player.uniqueId, player.name).getOrNull()!!
        economyManager.deposit(account.accountId, 1, BigDecimal("1234.56"))

        // 通貨フォーマット付き（デフォルト通貨 JPY: "%amount% %plural%"）
        assertEquals("1,234.56 円", render("<kerria_balance>", player))
        // 桁数 0 を指定すると小数点以下を切り捨てて表示する
        assertEquals("1,234 円", render("<kerria_balance:default:0>", player))
        // 通貨は ID・名前でも指定できる
        assertEquals("1234.56", render("<kerria_balance_raw:1>", player))
        assertEquals("1234", render("<kerria_balance_raw:JPY:0>", player))
        // デフォルト通貨の名前
        assertEquals("JPY", render("<kerria_currency_default>", player))
    }

    @Test
    @DisplayName("Players without an account show zero and unknown currencies show nothing")
    fun fallbacks() {
        val player = server.addPlayer("NoAccountPlaceholderPlayer")
        assertEquals("0", render("<kerria_balance_raw:default:0>", player))
        assertEquals("", render("<kerria_balance:UNKNOWN>", player))
    }
}
