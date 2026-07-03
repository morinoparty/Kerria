package party.morino.kerria.api.files

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError

/**
 * ユーザー向けメッセージを管理するインターフェース
 *
 * MiniMessage 形式のメッセージテンプレートを外部ファイルから読み込み、
 * プレースホルダを差し込んだ文字列を提供します。テンプレートは
 * [reloadMessages] でディスクから再読み込みできます。
 */
interface MessageManager {

    /**
     * メッセージキーに対応するテンプレートを取得し、プレースホルダを差し込む
     *
     * テンプレート内の `<key>` 形式のプレースホルダが、指定した値で置換されます。
     * 差し込む値は MiniMessage として再解釈されないようエスケープされるため、
     * 通貨名やエラーメッセージなどに `<` が含まれていてもタグ注入は起こりません。
     *
     * @param key メッセージキー（例: "balance.result"）
     * @param placeholders プレースホルダ名と値のペア（例: "amount" to "100 円"）
     * @return プレースホルダ差し込み済みの MiniMessage 文字列
     */
    fun get(key: String, vararg placeholders: Pair<String, String>): String

    /**
     * メッセージファイルをディスクから再読み込みする
     *
     * 読み込みに失敗した場合、既存のメッセージはそのまま維持されます。
     *
     * @return リロードの結果。成功時はUnit、失敗時は[KerriaError]
     */
    fun reloadMessages(): Either<KerriaError, Unit>
}
