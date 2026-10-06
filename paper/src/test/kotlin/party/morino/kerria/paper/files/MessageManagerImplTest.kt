package party.morino.kerria.paper.files

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.files.MessageManager
import party.morino.kerria.paper.KerriaTest
import java.io.File
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class MessageManagerImplTest : KoinTest {

    private val messageManager: MessageManager by inject()

    /** データフォルダ上の日本語バンドル */
    private val jaFile: File
        get() = File(File(KerriaTest.plugin.dataFolder, "translation"), "ja_JP.properties")

    /** Component を指定ロケールで解決し、プレーンテキストへ変換する */
    private fun render(component: Component, locale: Locale): String =
        PlainTextComponentSerializer.plainText().serialize(GlobalTranslator.render(component, locale))

    @Test
    @DisplayName("Message renders in the requested locale")
    fun rendersInRequestedLocale() {
        val component = messageManager.get("pay.success", "player" to "Steve", "amount" to "100")

        val ja = render(component, Locale.JAPAN)
        assertTrue(ja.contains("Steve"))
        assertTrue(ja.contains("送金"))

        val en = render(component, Locale.US)
        assertTrue(en.contains("Steve"))
        assertTrue(en.contains("Sent"))
    }

    @Test
    @DisplayName("Unknown locale falls back to the default locale")
    fun unknownLocaleFallsBackToDefault() {
        val component = messageManager.get("balance.fetch-failed")
        // バンドルの無いロケール（フランス語）はデフォルトロケール（ja）へフォールバックする
        assertEquals(render(component, Locale.JAPAN), render(component, Locale.FRENCH))
    }

    @Test
    @DisplayName("Missing key returns visible marker without throwing")
    fun missingKeyReturnsMarker() {
        val result = render(messageManager.get("no.such.key"), Locale.JAPAN)
        assertTrue(result.contains("no.such.key"))
    }

    @Test
    @DisplayName("Placeholder values are not parsed as MiniMessage")
    fun placeholderValuesAreNotParsed() {
        // 値に含まれる <red> は文字列として扱われ、MiniMessage タグにならない
        val result = render(messageManager.get("balance.result", "amount" to "<red>hack"), Locale.JAPAN)
        assertTrue(result.contains("<red>hack"))
    }

    @Test
    @DisplayName("Malformed bundle keeps previous messages and returns error")
    fun malformedBundleRetainsPrevious() {
        val original = jaFile.readText()
        try {
            // 不正なユニコードエスケープを書き込むと Properties.load が例外を投げる
            jaFile.writeText("bad=\\uZZZZ\n")
            val result = messageManager.reloadMessages()
            assertTrue(result.isLeft())
            // 直前に読み込まれていたメッセージが引き続き解決できることを確認
            assertTrue(render(messageManager.get("pay.self"), Locale.JAPAN).isNotEmpty())
        } finally {
            jaFile.writeText(original)
            messageManager.reloadMessages()
        }
    }

    @Test
    @DisplayName("Component placeholders keep their formatting")
    fun componentPlaceholdersAreEmbedded() {
        val component = messageManager.get(
            "pay.received-with-message",
            mapOf("message" to Component.text("ガラス代")),
            "player" to "Steve",
            "amount" to "100 円",
        )
        // 受取通知とメッセージが1行にまとまる
        assertEquals("Steveから100 円 送られてきました。 (メッセージ: ガラス代)", render(component, Locale.JAPAN))
    }

    @Test
    @DisplayName("Keys missing from the file fall back to bundled defaults")
    fun missingKeysFallBackToBundled() {
        val original = jaFile.readText()
        try {
            // 旧バージョンのファイルを想定し、既存キーのみを上書きしたファイルにする
            jaFile.writeText("pay.self=custom\n")
            messageManager.reloadMessages()
            assertEquals("custom", render(messageManager.get("pay.self"), Locale.JAPAN))
            // ファイルに無いキーは同梱の既定値で解決される
            assertTrue(render(messageManager.get("balance.fetch-failed"), Locale.JAPAN).isNotEmpty())
            assertTrue(!render(messageManager.get("balance.fetch-failed"), Locale.JAPAN).contains("Missing"))
        } finally {
            jaFile.writeText(original)
            messageManager.reloadMessages()
        }
    }
}
