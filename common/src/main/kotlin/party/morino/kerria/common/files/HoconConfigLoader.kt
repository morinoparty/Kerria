package party.morino.kerria.common.files

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigRenderOptions
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.hocon.Hocon
import java.io.File

/**
 * HOCON 形式の設定ファイルを読み書きするローダー
 *
 * Paper と Velocity の両方で同じ読み込み規則を使うために共通化している。
 * - 設定ファイルが無ければ、コメント付きの既定ファイル（リソース）をそのままコピーする
 * - 読み込み時はリソースの既定値をフォールバックにするので、古い設定ファイルに無いキーも既定値で補われる
 * - キー名は HOCON の慣習に合わせて kebab-case（例: `default-currency-id`）を使う
 *
 * @property classLoader 既定ファイル（リソース）を探すクラスローダー
 * @property resourceName 既定ファイルのリソース名（例: `config.conf`）
 */
@OptIn(ExperimentalSerializationApi::class)
class HoconConfigLoader(
    private val classLoader: ClassLoader,
    private val resourceName: String,
) {
    // camelCase のプロパティ名を kebab-case のキーへ対応付ける。書き戻し時に全項目を出すため既定値も出力する
    private val hocon = Hocon {
        useConfigNamingConvention = true
        encodeDefaults = true
    }

    /**
     * 設定ファイルを読み込む（無ければ既定ファイルを書き出してから読み込む）
     *
     * @param file 設定ファイル
     * @param serializer 設定クラスのシリアライザ
     * @return 読み込んだ設定
     * @throws com.typesafe.config.ConfigException HOCON の構文が不正な場合
     * @throws kotlinx.serialization.SerializationException 値の型が設定クラスと一致しない場合
     */
    fun <T> load(file: File, serializer: KSerializer<T>): T {
        // 初回起動時はコメント付きの既定ファイルを配置する
        if (!file.exists()) {
            writeDefault(file)
        }
        // ユーザーの設定にリソースの既定値を重ね、${?ENV} などの置換を解決する
        val config = ConfigFactory.parseFile(file, strictParseOptions())
            .withFallback(defaultConfig())
            .resolve()
        return hocon.decodeFromConfig(serializer, config)
    }

    /**
     * 設定の変更点をファイルへ書き戻す
     *
     * ファイル全体を描画し直すとキーの並びが変わり、`${?ENV}` で読み込んだ値（パスワード等）が
     * 平文で書き出されてしまうため、[previous] から [value] で値が変わったキーの行だけを書き換える。
     * ディスク上のファイルとの差分ではなく呼び出し側の変更点だけを書くので、
     * サーバー稼働中にファイルを手で編集していても、無関係なキーが巻き戻されることはない。
     * - 変更されたキーが「キー = 単一の値（# コメント）」の形の行としてファイル内に 1 つだけあれば、その値だけを置き換える
     * - それ以外（見つからない・複数ある・1 行に複数のキーがある等）は、ファイル末尾に `パス = 値` を追記する
     *   （HOCON では後の定義が優先される）
     * - 置き換え後の内容を読み直して意図どおりにならなければ、置き換えをやめて追記に切り替える
     *
     * @param file 設定ファイル
     * @param serializer 設定クラスのシリアライザ
     * @param previous 変更前の設定（現在メモリ上にある設定）
     * @param value 書き出す設定
     */
    fun <T> save(file: File, serializer: KSerializer<T>, previous: T, value: T) {
        // 初回はコメント付きの既定ファイルを元にする
        if (!file.exists()) {
            writeDefault(file)
        }
        // 呼び出し側が変更した葉のパスだけを書き込む対象にする
        val before = flatten(hocon.encodeToConfig(serializer, previous))
        val changed = flatten(hocon.encodeToConfig(serializer, value)).filter { (path, rendered) -> before[path] != rendered }
        if (changed.isEmpty()) return

        val original = file.readText()
        // まずは該当行の値だけを置き換え、置き換えられないキーは追記する
        val patched = changed.entries.fold(original) { text, (path, rendered) ->
            replaceValue(text, path, rendered) ?: appendValue(text, path, rendered)
        }
        // 置き換えで他のキーを壊していないか確認し、だめなら全て追記で書き込む
        val result = if (isExpected(original, patched, changed)) {
            patched
        } else {
            changed.entries.fold(original) { text, (path, rendered) -> appendValue(text, path, rendered) }
        }
        file.writeText(result)
    }

    /**
     * 書き換え後の内容が「変更したキーだけが新しい値になり、他のキーは元のまま」になっているかを確認する
     */
    private fun isExpected(original: String, patched: String, changed: Map<String, String>): Boolean {
        val before = runCatching { flatten(parseText(original)) }.getOrNull() ?: return false
        val after = runCatching { flatten(parseText(patched)) }.getOrNull() ?: return false
        return after == before + changed
    }

    /**
     * 設定ファイルの文字列を、リソースの既定値で補完したうえで読み込む（置換は環境変数も含めて解決する）
     */
    private fun parseText(text: String): Config =
        ConfigFactory.parseString(text).withFallback(defaultConfig()).resolve()

    /**
     * 設定を「パス → HOCON 表記の値」の平坦な表へ変換する
     */
    private fun flatten(config: Config): Map<String, String> =
        config.entrySet().associate { (path, value) -> path to value.render(ConfigRenderOptions.concise()) }

    /**
     * キーの行がファイル内に 1 つだけある場合、その行の値部分を置き換える
     *
     * 対象は「インデント・キー・区切り（= か :）・単一の値・行末コメント」だけで構成された行に限る。
     * 値の後ろに `,` や `}` など別の要素が続く行は、値の範囲を安全に特定できないため対象外とする。
     *
     * @return 置き換え後の文字列。対象の行が見つからない・複数ある場合は null
     */
    private fun replaceValue(text: String, path: String, rendered: String): String? {
        // 葉のキー名（例: economy.default-currency-id → default-currency-id）か、パスそのもので書かれた行を探す
        val leaf = path.substringAfterLast('.')
        val keys = listOf(path, leaf).distinct().joinToString("|") { Regex.escape(it) }
        // 値は「引用符付き文字列」か「空白・カンマ・括弧・引用符・コメント記号を含まない連続した文字」のどちらか
        val value = """"(?:[^"\\\n]|\\.)*"|[^\s,{}\[\]#"/]+"""
        val line = Regex(
            """^([ \t]*)($keys)([ \t]*[=:][ \t]*)(?:$value)([ \t]*(?:(?:#|//).*)?)\r?$""",
            RegexOption.MULTILINE,
        )
        val match = line.findAll(text).singleOrNull() ?: return null
        val (indent, key, separator, trailing) = match.destructured
        // 行末の \r は一致範囲に含まれるので、元の改行コードを保つため戻す
        val carriageReturn = if (match.value.endsWith("\r")) "\r" else ""
        return text.replaceRange(match.range, "$indent$key$separator$rendered$trailing$carriageReturn")
    }

    /**
     * ファイル末尾に `パス = 値` を追記する
     */
    private fun appendValue(text: String, path: String, rendered: String): String {
        val separator = if (text.endsWith("\n")) "" else "\n"
        return "$text$separator$path = $rendered\n"
    }

    /**
     * 既定ファイル（リソース）をそのままコピーする
     *
     * エンコード結果ではなく原文をコピーするのは、リソースに書いたコメントや並び順を保つため。
     */
    private fun writeDefault(file: File) {
        file.parentFile?.mkdirs()
        val stream = classLoader.getResourceAsStream(resourceName)
            ?: error("Default config resource '$resourceName' was not found")
        stream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
    }

    /**
     * リソースの既定ファイルを読み込む（読み込み時のフォールバック用）
     */
    private fun defaultConfig(): Config =
        ConfigFactory.parseResources(classLoader, resourceName, strictParseOptions())

    /**
     * ファイル（リソース）が存在しない場合に空として扱わず、エラーにする読み込みオプション
     */
    private fun strictParseOptions(): ConfigParseOptions =
        ConfigParseOptions.defaults().setAllowMissing(false)

    /**
     * 書き戻し時の出力形式（JSON ではなく HOCON で、コメントを残し、origin コメントは出さない）
     */
    private fun renderOptions(): ConfigRenderOptions = ConfigRenderOptions.defaults()
        .setJson(false)
        .setComments(true)
        .setOriginComments(false)
        .setFormatted(true)
}
