package party.morino.kerria.paper.files

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.bukkit.plugin.java.JavaPlugin
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.files.Config
import party.morino.kerria.api.files.ConfigManager
import java.io.File

/**
 * 設定管理機能の実装クラス
 *
 * YAML形式の設定ファイルの読み込み・保存・リロードを提供する。
 */
class ConfigManagerImpl(private val plugin: JavaPlugin) : ConfigManager {
    private val configFile = File(plugin.dataFolder, "config.yaml")
    private var currentConfig = Config()

    init {
        // プラグインのデータフォルダが存在しない場合は作成
        if (!plugin.dataFolder.exists()) {
            plugin.logger.info("Creating config...")
            plugin.dataFolder.mkdirs()
        }

        // 設定ファイルが存在しない場合はデフォルト設定を保存
        if (!configFile.exists()) {
            plugin.logger.info("Creating default config...")
            saveConfig()
        }

        // 設定を読み込む
        reloadConfig()
    }

    override fun getConfig(): Config = currentConfig

    override fun reloadConfig(): Either<KerriaError, Unit> = try {
        plugin.logger.info("Loading config...")
        val yaml = configFile.readText()
        currentConfig = Yaml.default.decodeFromString<Config>(yaml)
        plugin.logger.info("Config loaded.")
        Unit.right()
    } catch (e: Exception) {
        KerriaError.ConfigLoadError(e.message ?: "Unknown error").left()
    }

    private fun saveConfig() {
        val yaml = Yaml.default.encodeToString<Config>(currentConfig)
        configFile.writeText(yaml)
    }
}
