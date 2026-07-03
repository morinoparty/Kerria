package party.morino.kerria.paper.database

import com.zaxxer.hikari.HikariDataSource
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.common.database.DatabaseInitializer

/**
 * データベースの接続・テーブル作成を管理するクラス（Paper 用）
 *
 * 実際の接続・スキーマ作成はプラットフォーム非依存の [DatabaseInitializer] へ委譲する。
 */
class DatabaseManager(private val plugin: JavaPlugin) : KoinComponent {
    private val configManager: ConfigManager by inject()
    private var dataSource: HikariDataSource? = null

    /**
     * データベースを初期化する
     *
     * 設定ファイルに基づいてDB接続を確立し、全テーブルを作成する。
     */
    fun initialize() {
        val config = configManager.getConfig()
        val result = DatabaseInitializer.connect(config.database, plugin.dataFolder) { plugin.logger.info(it) }
        if (!result.success) {
            return
        }
        dataSource = result.dataSource
        DatabaseInitializer.createTables()
        DatabaseInitializer.seedDefaultCurrency(config.economy.currency) { plugin.logger.info(it) }
    }

    /**
     * データベース接続を安全に閉じる
     */
    fun shutdown() {
        dataSource?.close()
        dataSource = null
    }
}
