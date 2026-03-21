package party.morino.kerria.paper.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.files.DatabaseConfig
import party.morino.kerria.paper.database.table.AccountBalanceTable
import party.morino.kerria.paper.database.table.AccountTable
import party.morino.kerria.paper.database.table.CurrencyTable
import party.morino.kerria.paper.database.table.ExchangeRateTable
import party.morino.kerria.paper.database.table.TransactionLogTable

/**
 * データベースの接続・テーブル作成を管理するクラス
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
        val databaseConfig = configManager.getConfig().database
        if (!connect(databaseConfig)) {
            return
        }
        createTables()
        seedDefaultCurrency()
    }

    /**
     * データベース接続を安全に閉じる
     */
    fun shutdown() {
        dataSource?.close()
        dataSource = null
    }

    /**
     * DB接続を確立する
     *
     * SQLiteは直接接続、PostgreSQLはHikariCP接続プールを使用する。
     * @return 接続成功なら true、失敗なら false
     */
    private fun connect(config: DatabaseConfig): Boolean {
        when (config.mode) {
            "sqlite" -> {
                Database.connect(
                    "jdbc:sqlite:${plugin.dataFolder.resolve("${config.database}.db").absolutePath}",
                    "org.sqlite.JDBC",
                )
                plugin.logger.info("SQLite database connected!")
            }
            "postgresql" -> {
                val poolConfig = config.pool
                val hikariConfig = HikariConfig().apply {
                    jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
                    driverClassName = "org.postgresql.Driver"
                    username = config.username
                    password = config.password
                    maximumPoolSize = poolConfig.maximumPoolSize
                    minimumIdle = poolConfig.minimumIdle
                    connectionTimeout = poolConfig.connectionTimeout
                    idleTimeout = poolConfig.idleTimeout
                    maxLifetime = poolConfig.maxLifetime
                    poolName = "kerria-pool"
                }
                val ds = HikariDataSource(hikariConfig)
                dataSource = ds
                Database.connect(ds)
                plugin.logger.info(
                    "PostgreSQL database connected with HikariCP! (pool: ${poolConfig.maximumPoolSize} max, ${poolConfig.minimumIdle} idle)",
                )
            }
            else -> {
                plugin.logger.severe("Invalid database type: ${config.mode}. Plugin will not function correctly.")
                return false
            }
        }
        return true
    }

    /**
     * 全テーブルを作成し、既存テーブルに不足カラムがあれば追加する
     */
    private fun createTables() {
        val tables = arrayOf(AccountTable, CurrencyTable, AccountBalanceTable, TransactionLogTable, ExchangeRateTable)
        transaction {
            // テーブルが存在しなければ作成
            SchemaUtils.create(*tables)
            // 既存テーブルとの差分を検出し、マイグレーションSQLを実行
            val migrationStatements = MigrationUtils.statementsRequiredForDatabaseMigration(*tables, withLogs = true)
            migrationStatements.forEach { exec(it) }
        }
    }

    /**
     * デフォルト通貨がなければ設定から作成する
     *
     * シーケンス競合を避けるため、明示的IDの指定はせず insertAndGetId を使う。
     * 既に通貨が1つでも存在すればシード処理をスキップする。
     */
    private fun seedDefaultCurrency() {
        val currencyConfig = configManager.getConfig().economy.currency
        transaction {
            // 通貨が1つでも存在すればスキップ
            val hasAnyCurrency = CurrencyTable.selectAll().count() > 0
            if (hasAnyCurrency) return@transaction

            // デフォルト通貨を作成（IDは DB が自動採番）
            val generatedId = CurrencyTable.insertAndGetId {
                it[name] = currencyConfig.name
                it[symbol] = currencyConfig.symbol
                it[plural] = currencyConfig.plural
                it[format] = currencyConfig.format
                it[fractionalDigits] = currencyConfig.fractionalDigits
            }
            plugin.logger.info("Default currency '${currencyConfig.name}' created with id=${generatedId.value}.")
        }
    }
}
