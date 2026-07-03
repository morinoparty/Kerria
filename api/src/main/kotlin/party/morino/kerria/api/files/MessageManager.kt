package party.morino.kerria.api.files

import arrow.core.Either
import net.kyori.adventure.text.Component
import party.morino.kerria.api.error.KerriaError

/**
 * ユーザー向けメッセージを管理するインターフェース
 *
 * Adventure の翻訳フレームワークを利用したロケール対応のメッセージ生成を行います。
 * [get] が返す [Component] は「翻訳可能コンポーネント（translatable component）」であり、
 * 送信時に各受信者のクライアントロケールへ自動的に解決されます。
 * これにより、1つのメッセージがプレイヤーごとに異なる言語で表示されます。
 *
 * メッセージ本文は `translation/<locale>.properties`（例: `ja_JP`, `en_US`）で管理され、
 * MiniMessage 形式で記述します。プレースホルダは `<name>` 形式で埋め込み、
 * [get] の引数として名前と値のペアで渡します。
 */
interface MessageManager {

    /**
     * メッセージキーとプレースホルダから翻訳可能な [Component] を生成する
     *
     * 返り値は未解決の translatable component であり、送信時に受信者のロケールで解決されます。
     * プレースホルダの値は文字列として安全に埋め込まれ、MiniMessage タグとしては解釈されません
     * （タグ注入を防止します）。キーが存在しない場合は、視認できるフォールバックを返します。
     *
     * @param key メッセージキー（例: "pay.success"）
     * @param placeholders プレースホルダ名と値のペア（例: "player" to "Steve"）
     * @return 翻訳可能なコンポーネント、もしくは欠落キーのフォールバック
     */
    fun get(key: String, vararg placeholders: Pair<String, String>): Component

    /**
     * すべてのロケールバンドルをディスクから再読み込みする
     *
     * 読み込みに失敗した場合、既存のメッセージはそのまま維持されます。
     *
     * @return リロードの結果。成功時はUnit、失敗時は[KerriaError]
     */
    fun reloadMessages(): Either<KerriaError, Unit>
}
