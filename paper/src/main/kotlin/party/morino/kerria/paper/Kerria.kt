package party.morino.kerria.paper

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.common.KerriaCommon
import party.morino.kerria.paper.account.AccountManagerImpl
import party.morino.kerria.paper.currency.CurrencyManagerImpl
import party.morino.kerria.paper.economy.VaultEconomy
import party.morino.kerria.paper.files.ConfigManagerImpl
import party.morino.kerria.paper.log.LogManagerImpl

/**
 * Kerriaプラグインのメインクラス
 *
 * このクラスはプラグインのエントリーポイントとして機能し、各マネージャーの初期化とDIの設定を行います。
 * Koinを使用した依存性注入を実装し、各機能のマネージャーをシングルトンとして提供します。
 * また、データベースの初期化も担当します。
 *
 * @property accountManager アカウント管理機能のインスタンス
 * @property logManager 取引ログ管理機能のインスタンス
 * @property configManager 設定管理機能のインスタンス
 * @see KerriaAPI プラグインのAPI定義
 * @see AccountManager アカウント管理のインターフェース
 * @see LogManager 取引ログ管理のインターフェース
 * @see ConfigManager 設定管理のインターフェース
 */
open class Kerria : SuspendingJavaPlugin(), KerriaAPI {

    // 各マネージャーのインスタンス
    private lateinit var accountManager: AccountManager
    private lateinit var logManager: LogManager
    private lateinit var configManager: ConfigManager
    private lateinit var currencyManager: CurrencyManager

    /**
     * プラグインの有効化時に呼び出されるメソッド
     *
     * このメソッドでは以下の初期化処理を行います：
     * 1. Koinの初期化と依存性の設定
     * 2. 各マネージャーのインスタンス化
     * 3. データベースの初期化
     * 4. Vault Economyサービスの登録
     */
    override suspend fun onEnableAsync() {
        setupKoin()
        KerriaCommon.init()

        // マネージャーの初期化
        accountManager = GlobalContext.get().get()
        logManager = GlobalContext.get().get()
        configManager = GlobalContext.get().get()
        currencyManager = GlobalContext.get().get()

        // データベースの初期化
        initDatabase()

        // Vault Economyサービスの登録
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
     * テスト環境では既に初期化済みの場合があるため、モジュールの追加のみ行う
     */
    private fun setupKoin() {
        val appModule = module {
            single<Kerria> { this@Kerria }
            single<KerriaAPI> { this@Kerria }
            single<AccountManager> { AccountManagerImpl() }
            single<LogManager> { LogManagerImpl(this@Kerria) }
            single<ConfigManager> { ConfigManagerImpl(this@Kerria) }
            single<CurrencyManager> { CurrencyManagerImpl() }
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

    /**
     * データベースの初期化を行うメソッド
     *
     * このメソッドは設定ファイルに基づいて適切なデータベース接続を確立します。
     * 現在はSQLiteとPostgreSQLをサポートしています。
     *
     * - SQLite: プラグインのデータフォルダにデータベースファイルを作成
     * - PostgreSQL: 設定ファイルの接続情報に基づいて接続を確立
     */
    fun initDatabase() {
        // データベースの初期化処理
        val config = configManager.getConfig()
        val databaseConfig = config.database

        if (databaseConfig.database == "sqlite") {
            // SQLiteの初期化処理
            Database.connect(
                "jdbc:sqlite:${dataFolder.resolve("${databaseConfig.database}.db").absolutePath}",
                "org.sqlite.JDBC",
            )
            logger.info("SQLite database connected!")
        } else if (databaseConfig.database == "postgresql") {
            // PostgreSQLの初期化処理
            Database.connect(
                url = "jdbc:postgresql://${databaseConfig.host}:${databaseConfig.port}/${databaseConfig.database}",
                driver = "org.postgresql.Driver",
                user = databaseConfig.username,
                password = databaseConfig.password,
            )
            logger.info("PostgreSQL database connected!")
        } else {
            logger.warning("Invalid database type: ${databaseConfig.database}")
        }
    }

    /**
     * アカウント管理機能のインスタンスを取得します
     *
     * @return アカウント管理機能のインスタンス
     */
    override fun getAccountManager(): AccountManager = accountManager

    /**
     * 取引ログ管理機能のインスタンスを取得します
     *
     * @return 取引ログ管理機能のインスタンス
     */
    override fun getLogManager(): LogManager = logManager

    /**
     * 設定管理機能のインスタンスを取得します
     *
     * @return 設定管理機能のインスタンス
     */
    override fun getConfigManager(): ConfigManager = configManager

    /**
     * 通貨管理機能のインスタンスを取得します
     *
     * @return 通貨管理機能のインスタンス
     */
    override fun getCurrencyManager(): CurrencyManager = currencyManager
}
