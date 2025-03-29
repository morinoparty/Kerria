package party.morino.kerria.files

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.plugin.java.JavaPlugin
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.error.ConfigError
import party.morino.kerria.api.files.Config
import party.morino.kerria.api.files.ConfigManager
import java.io.File

/**
 * 設定管理機能の実装クラス
 *
 * このクラスは[ConfigManager]インターフェースを実装し、プラグインの設定ファイルの管理を担当します。
 * JSON形式の設定ファイルの読み込み、保存、再読み込みの機能を提供します。
 * 設定ファイルが存在しない場合は、デフォルト設定を自動的に作成します。
 *
 * @property plugin プラグインのインスタンス
 * @property configFile 設定ファイルのパス
 * @property currentConfig 現在読み込まれている設定
 * @see ConfigManager 設定管理のインターフェース定義
 * @see Config 設定情報を表すデータクラス
 */
class ConfigManagerImpl(private val plugin: JavaPlugin) : ConfigManager {
    private val configFile = File(plugin.dataFolder, "config.yaml")
    private var currentConfig = Config()

    /**
     * 初期化処理
     *
     * プラグインのデータフォルダと設定ファイルの存在確認を行い、
     * 必要に応じて作成します。その後、設定を読み込みます。
     */
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

    /**
     * 現在の設定を取得します
     *
     * このメソッドは現在メモリ上に読み込まれている設定を返します。
     * 設定ファイルが変更された場合は、[reloadConfig]を呼び出して再読み込みする必要があります。
     *
     * @return 現在適用されている設定
     */
    override fun getConfig(): Config = currentConfig

    /**
     * 設定ファイルを再読み込みします
     *
     * このメソッドは設定ファイルを読み込み、メモリ上の設定を更新します。
     * ファイルの読み込みに失敗した場合はエラーを返します。
     *
     * @return Either<KerriaError, Unit> 成功時はUnit、失敗時はエラー情報を含むEither
     * @throws ConfigError.LoadError 設定ファイルの読み込みに失敗した場合
     */
    override fun reloadConfig(): Either<KerriaError, Unit> = try {
        plugin.logger.info("Loading config...")
        val yaml = configFile.readText()
        currentConfig = Yaml.default.decodeFromString<Config>(yaml)
        plugin.logger.info("Config loaded.")

        Unit.right()
    } catch (e: Exception) {
        ConfigError.LoadError(e.message ?: "Unknown error").left()
    }

    /**
     * 現在の設定をファイルに保存します
     *
     * このメソッドは現在メモリ上にある設定をJSON形式でファイルに保存します。
     * 主に初期設定の作成時に使用されます。
     */
    private fun saveConfig() {
        val json = Json.encodeToString(currentConfig)
        configFile.writeText(json)
    }
} 