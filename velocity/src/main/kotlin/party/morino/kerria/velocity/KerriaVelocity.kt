package party.morino.kerria.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import party.morino.kerria.common.database.DatabaseInitializer
import party.morino.kerria.common.placeholder.KerriaPlaceholders
import party.morino.kerria.velocity.command.VKerriaCommand
import party.morino.kerria.velocity.economy.CrossServerEconomy
import party.morino.kerria.velocity.files.VelocityConfigManager
import party.morino.kerria.velocity.placeholder.VelocityPlaceholderBalanceSource
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
            val meta = proxy.commandManager.metaBuilder(VKerriaCommand.LABEL).plugin(this).build()
            proxy.commandManager.register(meta, VKerriaCommand(proxy, economy, config.defaultCurrencyId))

            // MiniPlaceholders 連携の登録（存在する場合のみ）
            registerPlaceholders(economy, config.defaultCurrencyId)

            logger.info("Kerria (Velocity) has been enabled.")
        }

        /**
         * MiniPlaceholders が存在する場合に Kerria のプレースホルダーを登録する
         *
         * プロキシ上のプラグイン（タブリストなど）から、共有データベースの残高を表示できるようにする。
         */
        private fun registerPlaceholders(economy: CrossServerEconomy, defaultCurrencyId: Int) {
            // MiniPlaceholders が無い環境では API のクラスを読み込まないよう、参照はこのガードの内側に閉じ込める
            if (!proxy.pluginManager.isLoaded(MINI_PLACEHOLDERS_ID)) {
                return
            }
            try {
                val version = proxy.pluginManager.ensurePluginContainer(this).description.version.orElse("unknown")
                KerriaPlaceholders.create(VelocityPlaceholderBalanceSource(economy, defaultCurrencyId), version).register()
                logger.info("Registered MiniPlaceholders expansion.")
            } catch (e: Exception) {
                // 連携の失敗は致命的ではないため、警告のみで続行する
                logger.warn("Failed to register MiniPlaceholders expansion: ${e.message}")
            }
        }

        private companion object {
            // MiniPlaceholders の Velocity プラグインID
            const val MINI_PLACEHOLDERS_ID = "miniplaceholders"
        }
    }
