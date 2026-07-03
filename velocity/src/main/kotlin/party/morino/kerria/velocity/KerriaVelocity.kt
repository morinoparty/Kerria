package party.morino.kerria.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import party.morino.kerria.common.database.DatabaseInitializer
import party.morino.kerria.velocity.command.KerriaCommand
import party.morino.kerria.velocity.economy.CrossServerEconomy
import party.morino.kerria.velocity.files.VelocityConfigManager
import java.nio.file.Path

/**
 * Kerria の Velocity プロキシプラグイン
 *
 * Paper 側と同じ共有データベース（PostgreSQL 推奨）へ接続し、
 * プロキシ上でクロスサーバーの残高照会・送金コマンドを提供する。
 */
class KerriaVelocity
    @Inject
    constructor(
        private val proxy: ProxyServer,
        private val logger: Logger,
        @DataDirectory private val dataDirectory: Path,
    ) {

        @Subscribe
        fun onProxyInitialize(event: ProxyInitializeEvent) {
            // 設定を読み込み、共有データベースへ接続する
            val config = VelocityConfigManager.load(dataDirectory, logger)
            val result = DatabaseInitializer.connect(config.database, dataDirectory.toFile()) { logger.info(it) }
            if (!result.success) {
                logger.error("Failed to connect to the database. Cross-server economy is disabled.")
                return
            }
            // スキーマを作成（既に存在すれば差分のみ）
            DatabaseInitializer.createTables()

            // コマンドを登録する
            val economy = CrossServerEconomy(config.defaultCurrencyId)
            val meta = proxy.commandManager.metaBuilder("kerria").plugin(this).build()
            proxy.commandManager.register(meta, KerriaCommand(proxy, economy, config.defaultCurrencyId))

            logger.info("Kerria (Velocity) has been enabled.")
        }
    }
