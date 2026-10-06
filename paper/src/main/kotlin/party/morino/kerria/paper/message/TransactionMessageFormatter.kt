package party.morino.kerria.paper.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

/**
 * 取引メッセージ（`--message` で指定するメモ）の保存形式への変換と表示を担うヘルパー
 *
 * 取引メッセージは MiniMessage 形式の文字列として取引ログに保存する。
 * - プレイヤーが指定したメッセージ（/pay）はタグをエスケープして保存し、プレーンテキストとして扱う
 * - 管理者が指定したメッセージ（/kerria give など）はそのまま保存し、MiniMessage として扱う
 *
 * 保存形式を MiniMessage に統一することで、取引履歴ではどちらも同じ方法で表示できる。
 */
object TransactionMessageFormatter {

    /** 取引ログの message カラム（varchar(512)）に収まる最大文字数 */
    const val MAX_LENGTH = 512

    // 全タグを解釈する MiniMessage（管理者のメッセージの通知に使う）
    private val fullMiniMessage = MiniMessage.miniMessage()

    // 装飾系タグのみを解釈する MiniMessage（取引履歴の表示に使う）
    // click / hover などは、他プラグインが記録したメッセージ経由で閲覧者を誘導できるため許可しない
    private val styleOnlyMiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.resolver(
                StandardTags.color(),
                StandardTags.decorations(),
                StandardTags.gradient(),
                StandardTags.rainbow(),
                StandardTags.reset(),
            ),
        )
        .build()

    /**
     * プレイヤーが入力したプレーンテキストを保存形式に変換する
     *
     * MiniMessage のタグをエスケープし、表示時にタグとして解釈されないようにする。
     *
     * @param raw プレイヤーが入力したメッセージ
     * @return 保存形式（エスケープ済み MiniMessage）の文字列
     */
    fun fromPlainText(raw: String): String = fullMiniMessage.escapeTags(raw.trim())

    /**
     * 管理者が入力した MiniMessage を保存形式に変換する
     *
     * @param raw 管理者が入力した MiniMessage 文字列
     * @return 保存形式の文字列（前後の空白のみ除去する）
     */
    fun fromMiniMessage(raw: String): String = raw.trim()

    /**
     * 保存形式の文字列が取引ログに保存できる長さかどうかを判定する
     *
     * @param stored 保存形式の文字列
     * @return 空でなく最大長以下であれば true
     */
    fun isValid(stored: String): Boolean = stored.isNotEmpty() && stored.length <= MAX_LENGTH

    /**
     * 保存形式の文字列を、全タグを解釈してコンポーネントに変換する
     *
     * 管理者が指定したメッセージを受取人へ通知する際に使う。
     *
     * @param stored 保存形式の文字列
     * @return 装飾済みのコンポーネント
     */
    fun render(stored: String): Component = fullMiniMessage.deserialize(stored)

    /**
     * 保存形式の文字列を、装飾系タグのみ解釈してコンポーネントに変換する
     *
     * 取引履歴など、記録元を問わずメッセージを表示する際に使う。
     *
     * @param stored 保存形式の文字列
     * @return 装飾済みのコンポーネント
     */
    fun renderStyleOnly(stored: String): Component = styleOnlyMiniMessage.deserialize(stored)
}
