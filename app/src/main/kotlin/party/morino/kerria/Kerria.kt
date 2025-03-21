package party.morino.kerria

import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.koin.core.context.startKoin
import org.koin.dsl.module
import party.morino.kerria.account.AccountManagerImpl
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.economy.VaultEconomy
import party.morino.kerria.files.ConfigManagerImpl
import party.morino.kerria.log.LogManagerImpl
import sun.jvm.hotspot.HelloWorld.e

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
class Kerria : JavaPlugin(), KerriaAPI {

    // 各マネージャーのインスタンス
    private lateinit var accountManager: AccountManager
    private lateinit var logManager: LogManager
    private lateinit var configManager: ConfigManager

    /**
     * プラグインの有効化時に呼び出されるメソッド
     *
     * このメソッドでは以下の初期化処理を行います：
     * 1. Koinの初期化と依存性の設定
     * 2. 各マネージャーのインスタンス化
     * 3. データベースの初期化
     */
    override fun onEnable() {
        // Koinの初期化
        startKoin {
            modules(module {
                single<AccountManager> { AccountManagerImpl() }
                single<LogManager> { LogManagerImpl(this@Kerria) }
                single<ConfigManager> { ConfigManagerImpl(this@Kerria) }
            })
        }

        // マネージャーの初期化
        accountManager = org.koin.core.context.GlobalContext.get().get()
        logManager = org.koin.core.context.GlobalContext.get().get()
        configManager = org.koin.core.context.GlobalContext.get().get()

        // データベースの初期化
        initDatabase()

        server.servicesManager.register<Economy>(Economy::class.java, VaultEconomy(), this, ServicePriority.Highest)
        logger.info("Kerria has been enabled!")
    }

    /**
     * プラグインの無効化時に呼び出されるメソッド
     *
     * このメソッドではプラグインの終了処理を行います。
     * 現在は単純なログ出力のみを行っています。
     */
    override fun onDisable() {
        logger.info("Kerria has been disabled!")
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

        if(databaseConfig.database == "sqlite") {
            // SQLiteの初期化処理
            Database.connect("jdbc:sqlite:${dataFolder.resolve("${databaseConfig.database}.db").absolutePath}", "org.sqlite.JDBC")
            logger.info("SQLite database connected!")
        } else if(databaseConfig.database == "postgresql") {
            // PostgreSQLの初期化処理
            Database.connect(
                url = "jdbc:postgresql://${databaseConfig.host}:${databaseConfig.port}/${databaseConfig.database}",
                driver = "org.postgresql.Driver",
                user = databaseConfig.username,
                password = databaseConfig.password
            )
            logger.info("PostgreSQL database connected!")
        }else{
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
}
