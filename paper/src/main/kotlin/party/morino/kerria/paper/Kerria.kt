package party.morino.kerria.paper

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.paper.account.AccountManagerImpl
import party.morino.kerria.paper.currency.CurrencyManagerImpl
import party.morino.kerria.paper.database.DatabaseManager
import party.morino.kerria.paper.database.repository.AccountRepository
import party.morino.kerria.paper.database.repository.CurrencyRepository
import party.morino.kerria.paper.database.repository.TransactionLogRepository
import party.morino.kerria.paper.economy.EconomyManagerImpl
import party.morino.kerria.paper.economy.VaultEconomy
import party.morino.kerria.paper.files.ConfigManagerImpl
import party.morino.kerria.paper.log.LogManagerImpl

/**
 * Kerriaプラグインのメインクラス
 *
 * DI設定、DB初期化、Vault登録を行うエントリーポイント。
 */
open class Kerria : SuspendingJavaPlugin(), KerriaAPI {

    private lateinit var accountManager: AccountManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var economyManager: EconomyManager
    private lateinit var logManager: LogManager

    override suspend fun onEnableAsync() {
        // DI設定
        setupKoin()

        // マネージャーの取得
        accountManager = GlobalContext.get().get()
        currencyManager = GlobalContext.get().get()
        economyManager = GlobalContext.get().get()
        logManager = GlobalContext.get().get()

        // データベースの初期化
        val databaseManager: DatabaseManager = GlobalContext.get().get()
        databaseManager.initialize()

        // Vault Economy サービスの登録
        server.servicesManager.register<Economy>(
            Economy::class.java,
            VaultEconomy(),
            this,
            ServicePriority.Highest,
        )

        logger.info("${pluginMeta.name} v${pluginMeta.version} has been enabled!")
    }

    override suspend fun onDisableAsync() {
        logger.info("${pluginMeta.name} has been disabled!")
    }

    /**
     * Koin DI コンテナの初期化
     */
    private fun setupKoin() {
        val appModule = module {
            // プラグイン本体
            single<Kerria> { this@Kerria }
            single<KerriaAPI> { this@Kerria }

            // 設定
            single<ConfigManager> { ConfigManagerImpl(this@Kerria) }

            // リポジトリ
            single { AccountRepository() }
            single { CurrencyRepository() }
            single { TransactionLogRepository() }

            // DB管理
            single { DatabaseManager(this@Kerria) }

            // マネージャー
            single<AccountManager> { AccountManagerImpl() }
            single<CurrencyManager> { CurrencyManagerImpl() }
            single<LogManager> { LogManagerImpl() }
            single<EconomyManager> { EconomyManagerImpl() }
        }

        // 既存の Koin がある場合はモジュールを追加、なければ新規開始
        val koin = getOrNull()
        if (koin != null) {
            koin.loadModules(listOf(appModule))
        } else {
            GlobalContext.startKoin {
                modules(appModule)
            }
        }
    }

    override fun getAccountManager(): AccountManager = accountManager
    override fun getCurrencyManager(): CurrencyManager = currencyManager
    override fun getEconomyManager(): EconomyManager = economyManager
    override fun getLogManager(): LogManager = logManager
}
