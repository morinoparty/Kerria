package party.morino.kerria.paper.files

/**
 * メッセージテンプレートにプレースホルダを差し込む純粋なユーティリティ
 *
 * `<key>` 形式のプレースホルダを1回のスキャンで置換する。差し込んだ値を
 * 再スキャンしないため、値の中に別のプレースホルダ名が含まれても二重置換は
 * 起こらない。プレースホルダ表に無いトークン（`<red>` や `<click:...>` などの
 * MiniMessage タグ）はそのまま残す。外部依存の無い純粋オブジェクト。
 */
object MessageFormatter {

    // プレースホルダのトークンにマッチする正規表現（英数・ドット・ハイフン・アンダースコア）
    private val TOKEN = Regex("<([A-Za-z0-9_.-]+)>")

    /**
     * テンプレートのプレースホルダを差し込む
     *
     * @param template メッセージテンプレート（MiniMessage 文字列）
     * @param placeholders プレースホルダ名と値のペア
     * @return 差し込み済みの文字列
     */
    fun format(template: String, placeholders: Array<out Pair<String, String>>): String {
        // 置換対象を素早く引けるようマップ化する
        val values = placeholders.toMap()
        // 1回のスキャンで、表に載っているトークンのみ置換する
        return TOKEN.replace(template) { match ->
            val name = match.groupValues[1]
            // 対応する値があれば置換、無ければ元のトークン（MiniMessage タグ等）を維持
            values[name] ?: match.value
        }
    }
}
