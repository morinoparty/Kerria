package party.morino.kerria.paper.files

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * プレースホルダ差し込みユーティリティの単体テスト（純粋ロジック）
 */
class MessageFormatterTest {

    private fun format(template: String, vararg pairs: Pair<String, String>): String =
        MessageFormatter.format(template, pairs)

    @Test
    @DisplayName("Replaces single placeholder")
    fun replacesSinglePlaceholder() {
        val result = format("<green>残高は<amount>です。", "amount" to "100 円")
        assertEquals("<green>残高は100 円です。", result)
        assertFalse(result.contains("<amount>"))
    }

    @Test
    @DisplayName("Replaces multiple placeholders independently")
    fun replacesMultiplePlaceholders() {
        val result = format("<player>に<amount>を送金", "player" to "Steve", "amount" to "50 円")
        assertEquals("Steveに50 円を送金", result)
    }

    @Test
    @DisplayName("Leaves unknown tags untouched")
    fun leavesUnknownTagsUntouched() {
        // MiniMessage の色タグや click タグはプレースホルダ表に無いのでそのまま残る
        val result = format("<red><amount><click:run_command:'/x'>", "amount" to "5")
        assertEquals("<red>5<click:run_command:'/x'>", result)
    }

    @Test
    @DisplayName("Inserted value is not re-scanned")
    fun insertedValueNotRescanned() {
        // <a> の値 "<b>" が再スキャンされず、テンプレートの <b> のみ置換されることを確認
        val result = format("<a><b>", "a" to "<b>", "b" to "X")
        assertEquals("<b>X", result)
    }
}
