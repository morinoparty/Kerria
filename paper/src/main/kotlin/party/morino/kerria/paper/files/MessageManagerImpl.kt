package party.morino.kerria.paper.files

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.files.MessageManager
import java.io.File

/**
 * メッセージ管理機能の実装クラス
 *
 * `messages.yml`（キー → MiniMessage テンプレートのフラットなマップ）を読み込み、
 * プレースホルダを差し込んだ文字列を提供する。構造は [ConfigManagerImpl] に倣う。
 */
class MessageManagerImpl(private val plugin: JavaPlugin) : MessageManager {
    private val messagesFile = File(plugin.dataFolder, "messages.yml")

    // 現在読み込まれているメッセージ（デコード成功時のみ差し替える）
    private var messages: Map<String, String> = DefaultMessages.DEFAULT

    // 差し込む値をエスケープするための MiniMessage インスタンス
    private val miniMessage = MiniMessage.miniMessage()

    init {
        // データフォルダが無ければ作成する
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        // メッセージファイルが無ければデフォルトを書き出す
        if (!messagesFile.exists()) {
            plugin.logger.info("Creating default messages...")
            saveDefaultMessages()
        }

        // メッセージを読み込む
        reloadMessages()
    }

    override fun get(key: String, vararg placeholders: Pair<String, String>): String {
        // キーに対応するテンプレートを引く
        val template = messages[key] ?: run {
            // 欠落キーは警告を出し、視認できるフォールバックを返す
            plugin.logger.warning("Missing message key: $key")
            return "<red>Missing message: $key"
        }
        // 差し込む値を MiniMessage としてエスケープし、タグ注入を防ぐ
        val escaped = placeholders
            .map { (name, value) -> name to miniMessage.escapeTags(value) }
            .toTypedArray()
        return MessageFormatter.format(template, escaped)
    }

    override fun reloadMessages(): Either<KerriaError, Unit> = try {
        val yaml = messagesFile.readText()
        // デコードが成功した場合のみ差し替える（失敗時は既存のメッセージを維持）
        messages = Yaml.default.decodeFromString<Map<String, String>>(yaml)
        Unit.right()
    } catch (e: Exception) {
        KerriaError.ConfigLoadError(e.message ?: "Unknown error").left()
    }

    /** デフォルトのメッセージを YAML として書き出す */
    private fun saveDefaultMessages() {
        val yaml = Yaml.default.encodeToString<Map<String, String>>(DefaultMessages.DEFAULT)
        messagesFile.writeText(yaml)
    }
}
