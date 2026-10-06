package party.morino.kerria.paper.files

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.translation.Argument
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.files.MessageManager
import java.io.File
import java.util.Locale
import java.util.Properties

/**
 * メッセージ管理機能の実装クラス
 *
 * Adventure の [MiniMessageTranslationStore] を [GlobalTranslator] に登録することで、
 * ロケール対応の翻訳可能メッセージを提供する。[get] は未解決の translatable component を返し、
 * 送信時に受信者のクライアントロケールへ自動的に解決される。
 *
 * メッセージは `translation/<locale>.properties`（例: `ja_JP`, `en_US`）で管理し、
 * 初回起動時にバンドル済みのデフォルトをデータフォルダへ書き出す。以降はディスク上の
 * ファイルを読み込むため、サーバー管理者が編集できる。
 */
class MessageManagerImpl(private val plugin: JavaPlugin) : MessageManager, KoinComponent {

    // デフォルトロケール解決のため設定を参照する
    private val configManager: ConfigManager by inject()

    // 翻訳ファイルの配置ディレクトリ
    private val translationDir = File(plugin.dataFolder, "translation")

    // 登録済みのキー集合。欠落キーのフォールバック判定に用いる
    private var knownKeys: Set<String> = emptySet()

    init {
        // データフォルダ・翻訳ディレクトリが無ければ作成する
        if (!translationDir.exists()) {
            translationDir.mkdirs()
        }
        // 初回ロード（失敗しても致命的ではないため警告のみ）
        load().onLeft { plugin.logger.warning("Failed to load messages: ${it.message}") }
    }

    override fun get(key: String, vararg placeholders: Pair<String, String>): Component {
        // 未登録のキーは警告を出し、視認できるフォールバックを返す
        if (key !in knownKeys) {
            plugin.logger.warning("Missing message key: $key")
            return Component.text("Missing message: $key").color(NamedTextColor.RED)
        }
        // 値は文字列引数として安全に埋め込む（MiniMessage タグとして解釈されない）
        val args = placeholders.map { (name, value) -> Argument.string(name, value) }
        // 未解決の翻訳可能コンポーネントを返す（送信時に受信者ロケールで解決される）
        return Component.translatable(key).arguments(args)
    }

    override fun get(
        key: String,
        components: Map<String, ComponentLike>,
        vararg placeholders: Pair<String, String>,
    ): Component {
        // 文字列プレースホルダのみで組み立てた結果を土台にする（欠落キーの扱いも共通化する）
        val base = get(key, *placeholders)
        if (base !is TranslatableComponent) {
            return base
        }
        // コンポーネント引数は装飾を保ったまま埋め込む
        val componentArgs = components.map { (name, value) -> Argument.component(name, value) }
        return base.arguments(base.arguments() + componentArgs)
    }

    override fun reloadMessages(): Either<KerriaError, Unit> = load()

    /**
     * 全ロケールのバンドルを読み込み、成功時のみ GlobalTranslator のソースを差し替える
     *
     * 読み込みに失敗した場合は差し替えを行わず、直前のメッセージを維持する。
     */
    private fun load(): Either<KerriaError, Unit> = try {
        // 新しいストアを構築してから差し替えることで、失敗時に旧状態を保持する
        val newStore = MiniMessageTranslationStore.create(Key.key("kerria", "messages"))
        newStore.defaultLocale(resolveDefaultLocale())
        val keys = mutableSetOf<String>()
        BUNDLED_LOCALES.forEach { (locale, fileName) ->
            loadProperties(fileName).forEach { (rawKey, rawValue) ->
                val key = rawKey.toString()
                newStore.register(key, locale, rawValue.toString())
                keys += key
            }
        }

        // ここまで到達したら成功。旧ソースを解除して新ソースを登録する。
        // GlobalTranslator は JVM グローバルかつ同一 Key の二重登録を許さないため、
        // 登録済みストアを companion で追跡し、必ず解除してから登録する。
        registeredStore?.let { GlobalTranslator.translator().removeSource(it) }
        GlobalTranslator.translator().addSource(newStore)
        registeredStore = newStore
        knownKeys = keys
        Unit.right()
    } catch (e: Exception) {
        KerriaError.ConfigLoadError(e.message ?: "Unknown error").left()
    }

    /**
     * 指定ロケールの properties を読み込む（無ければバンドル済みリソースから書き出す）
     *
     * バンドル済みの値を土台にディスク上の値で上書きする。これにより、プラグイン更新で
     * 追加されたキーは、既存のファイルを持つサーバーでもバンドルの既定値で表示される。
     */
    private fun loadProperties(fileName: String): Properties {
        val file = File(translationDir, "$fileName.properties")
        if (!file.exists()) {
            // クラスパス上のリソースからデータフォルダへコピーする（UTF-8 で保存されている）
            javaClass.getResourceAsStream("/translation/$fileName.properties")?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
        // バンドル済みの既定値を先に読み込む
        val merged = Properties().apply {
            javaClass.getResourceAsStream("/translation/$fileName.properties")?.use { input ->
                load(input.reader(Charsets.UTF_8))
            }
        }
        // ディスク上の値（管理者の編集内容）で上書きする
        if (file.exists()) {
            file.inputStream().use { input -> merged.load(input.reader(Charsets.UTF_8)) }
        }
        return merged
    }

    /**
     * 設定のデフォルトロケール文字列を、バンドル済みロケールに解決する（不明なら日本語）
     *
     * バンドルは [Locale.JAPAN]（ja_JP）等の完全なロケールで登録するため、
     * フォールバックが正しく機能するよう、設定値の言語が一致する登録済みロケールを選ぶ。
     */
    private fun resolveDefaultLocale(): Locale {
        val parsed = Locale.forLanguageTag(configManager.getConfig().defaultLocale.replace('_', '-'))
        return BUNDLED_LOCALES.keys.firstOrNull { it.language == parsed.language } ?: Locale.JAPAN
    }

    companion object {
        // バンドルするロケールと、対応する properties ファイル名（Locale.toString() に一致）
        private val BUNDLED_LOCALES: Map<Locale, String> = linkedMapOf(
            Locale.JAPAN to "ja_JP",
            Locale.US to "en_US",
        )

        // GlobalTranslator に登録中の Kerria ストア（JVM グローバルに一意）
        private var registeredStore: MiniMessageTranslationStore? = null
    }
}
