package party.morino.kerria.paper.files

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.files.MessageManager
import party.morino.kerria.paper.KerriaTest
import java.io.File
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class MessageManagerImplTest : KoinTest {

    private val messageManager: MessageManager by inject()

    /** MockBukkit のデータフォルダ上の messages.yml */
    private val messagesFile: File
        get() = File(KerriaTest.plugin.dataFolder, "messages.yml")

    @Test
    @DisplayName("Get returns formatted default message")
    fun getReturnsFormattedDefault() {
        val result = messageManager.get("pay.success", "player" to "Steve", "amount" to "100 円")
        assertTrue(result.contains("Steve"))
        assertTrue(result.contains("100 円"))
        assertTrue(!result.contains("<player>"))
    }

    @Test
    @DisplayName("Missing key returns marker without throwing")
    fun missingKeyReturnsMarker() {
        val result = messageManager.get("no.such.key")
        assertTrue(result.contains("no.such.key"))
    }

    @Test
    @DisplayName("Placeholder values are escaped against tag injection")
    fun placeholderValuesAreEscaped() {
        // 値に含まれる <red> はエスケープされ、MiniMessage として解釈されない
        val result = messageManager.get("balance.result", "amount" to "<red>hack")
        assertTrue(result.contains("\\<red>"))
    }

    @Test
    @DisplayName("Reload picks up on-disk edits then restores")
    fun reloadPicksUpEdits() {
        val original = messagesFile.readText()
        try {
            // balance.result を書き換えて再読み込みし、反映されることを確認
            messagesFile.writeText("balance.result: \"<green>CHANGED <amount>\"\n")
            val reloaded = messageManager.reloadMessages()
            assertTrue(reloaded.isRight())
            assertTrue(messageManager.get("balance.result", "amount" to "1").contains("CHANGED"))
        } finally {
            // 他テストに影響しないよう元の内容へ戻す
            messagesFile.writeText(original)
            messageManager.reloadMessages()
        }
    }

    @Test
    @DisplayName("Malformed YAML returns error and keeps previous messages")
    fun malformedYamlReturnsError() {
        val original = messagesFile.readText()
        try {
            // 不正なYAMLを書き込むとLeftを返し、既存メッセージは維持される
            messagesFile.writeText(":\n  - not: valid: yaml: [")
            val result = messageManager.reloadMessages()
            assertTrue(result.isLeft())
            // 直前の正常なメッセージが引き続き使えることを確認
            assertTrue(messageManager.get("pay.self").isNotEmpty())
        } finally {
            messagesFile.writeText(original)
            messageManager.reloadMessages()
        }
    }
}
