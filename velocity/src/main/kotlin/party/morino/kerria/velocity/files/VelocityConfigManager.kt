package party.morino.kerria.velocity.files

import org.slf4j.Logger
import party.morino.kerria.common.files.HoconConfigLoader
import java.nio.file.Path

/**
 * Velocity の設定ファイル（config.conf）の読み込みを行う
 */
object VelocityConfigManager {
    // 設定ファイル名
    private const val CONFIG_FILE_NAME = "config.conf"
    // コメント付きの既定ファイルのリソース名（他の JAR の config.conf と衝突しないよう名前空間を付ける）
    private const val DEFAULT_CONFIG_RESOURCE = "kerria/config.conf"

    /**
     * データディレクトリから設定を読み込む（無ければコメント付きの既定ファイルを書き出す）
     */
    fun load(dataDirectory: Path, logger: Logger): VelocityConfig {
        val file = dataDirectory.resolve(CONFIG_FILE_NAME).toFile()
        // 既定ファイルはプラグイン JAR 内のリソースから読み込む
        val loader = HoconConfigLoader(javaClass.classLoader, DEFAULT_CONFIG_RESOURCE)
        return try {
            loader.load(file, VelocityConfig.serializer())
        } catch (e: Exception) {
            // 読み込み失敗時はデフォルトで継続する
            logger.warn("Failed to load $CONFIG_FILE_NAME, using defaults: ${e.message}")
            VelocityConfig()
        }
    }
}
