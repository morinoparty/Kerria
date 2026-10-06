package party.morino.kerria.paper.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransactionMessageFormatterTest {

    /** コンポーネントをプレーンテキストへ変換する */
    private fun plain(component: Component): String = PlainTextComponentSerializer.plainText().serialize(component)

    /** コンポーネントツリー内にクリックイベントが含まれるかを判定する */
    private fun hasClickEvent(component: Component): Boolean =
        component.clickEvent() != null || component.children().any { hasClickEvent(it) }

    @Test
    @DisplayName("Plain text keeps tags as literal text")
    fun plainTextKeepsTagsLiteral() {
        val stored = TransactionMessageFormatter.fromPlainText("<red>ガラス代")
        // エスケープ済みのため、表示時もタグとして解釈されない
        assertEquals("<red>ガラス代", plain(TransactionMessageFormatter.renderStyleOnly(stored)))
    }

    @Test
    @DisplayName("MiniMessage is rendered for admin messages")
    fun miniMessageIsRendered() {
        val rendered = TransactionMessageFormatter.render(TransactionMessageFormatter.fromMiniMessage("<red>reward"))
        assertEquals("reward", plain(rendered))
        assertEquals(NamedTextColor.RED, rendered.children().firstOrNull()?.color() ?: rendered.color())
    }

    @Test
    @DisplayName("Style-only rendering ignores click tags")
    fun styleOnlyIgnoresClickTags() {
        val rendered = TransactionMessageFormatter.renderStyleOnly("<click:run_command:/op me>hi</click>")
        assertFalse(hasClickEvent(rendered))
    }

    @Test
    @DisplayName("Messages longer than the column size are invalid")
    fun tooLongMessageIsInvalid() {
        assertTrue(TransactionMessageFormatter.isValid("a".repeat(TransactionMessageFormatter.MAX_LENGTH)))
        assertFalse(TransactionMessageFormatter.isValid("a".repeat(TransactionMessageFormatter.MAX_LENGTH + 1)))
        assertFalse(TransactionMessageFormatter.isValid(TransactionMessageFormatter.fromPlainText("   ")))
    }
}
