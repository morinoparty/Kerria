package party.morino.kerria.velocity.files

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.slf4j.Logger
import java.nio.file.Path

/**
 * Velocity の設定ファイル（config.yaml）の読み書きを行う
 */
object VelocityConfigManager {

    /**
     * データディレクトリから設定を読み込む（無ければデフォルトを書き出す）
     */
    fun load(dataDirectory: Path, logger: Logger): VelocityConfig {
        val dir = dataDirectory.toFile()
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = dir.resolve("config.yaml")
        if (!file.exists()) {
            file.writeText(Yaml.default.encodeToString(VelocityConfig()))
            logger.info("Created default config.yaml")
        }
        return try {
            Yaml.default.decodeFromString<VelocityConfig>(file.readText())
        } catch (e: Exception) {
            // 読み込み失敗時はデフォルトで継続する
            logger.warn("Failed to load config.yaml, using defaults: ${e.message}")
            VelocityConfig()
        }
    }
}
