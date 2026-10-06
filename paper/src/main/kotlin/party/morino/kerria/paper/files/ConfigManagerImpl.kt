package party.morino.kerria.paper.files

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.bukkit.plugin.java.JavaPlugin
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.files.Config
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.common.files.HoconConfigLoader
import java.io.File

/**
 * 設定管理機能の実装クラス
 *
 * HOCON形式の設定ファイル（config.conf）の読み込み・保存・リロードを提供する。
 */
class ConfigManagerImpl(private val plugin: JavaPlugin) : ConfigManager {
    private val configFile = File(plugin.dataFolder, CONFIG_FILE_NAME)
    // 既定ファイルはプラグイン JAR 内のリソースから読み込む
    private val loader = HoconConfigLoader(javaClass.classLoader, CONFIG_FILE_NAME)
    private var currentConfig = Config()

    init {
        // プラグインのデータフォルダが存在しない場合は作成
        if (!plugin.dataFolder.exists()) {
            plugin.logger.info("Creating config...")
            plugin.dataFolder.mkdirs()
        }

        // 設定を読み込む（ファイルが無ければローダーがコメント付きの既定ファイルを配置する）
        reloadConfig().onLeft { error ->
            // 読み込めない場合は既定値のまま起動し、原因をログに残す
            plugin.logger.severe("Failed to load $CONFIG_FILE_NAME, using defaults: ${error.message}")
        }
    }

    override fun getConfig(): Config = currentConfig

    override fun reloadConfig(): Either<KerriaError, Unit> = try {
        plugin.logger.info("Loading config...")
        currentConfig = loader.load(configFile, Config.serializer())
        plugin.logger.info("Config loaded.")
        Unit.right()
    } catch (e: Exception) {
        KerriaError.ConfigLoadError(e.message ?: "Unknown error").left()
    }

    override fun updateConfig(config: Config): Either<KerriaError, Unit> = try {
        // 現在の設定からの変更点だけをディスクへ書き出し、成功したらメモリ上の設定を差し替える
        loader.save(configFile, Config.serializer(), currentConfig, config)
        currentConfig = config
        Unit.right()
    } catch (e: Exception) {
        KerriaError.ConfigLoadError(e.message ?: "Unknown error").left()
    }

    private companion object {
        // 設定ファイル名（既定ファイルのリソース名も兼ねる）
        const val CONFIG_FILE_NAME = "config.conf"
    }
}
