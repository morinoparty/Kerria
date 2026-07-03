package party.morino.kerria.common.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import party.morino.kerria.api.files.CurrencyConfig
import party.morino.kerria.api.files.DatabaseConfig
import party.morino.kerria.common.database.table.AccountBalanceTable
import party.morino.kerria.common.database.table.AccountTable
import party.morino.kerria.common.database.table.BankMemberTable
import party.morino.kerria.common.database.table.CurrencyTable
import party.morino.kerria.common.database.table.ExchangeRateTable
import party.morino.kerria.common.database.table.TransactionLogTable
import java.io.File

/**
 * データベースの接続・スキーマ作成を行うプラットフォーム非依存のヘルパー
 *
 * Paper・Velocity の両プラグインが同じ共有データベースへ接続するために利用する。
 * ログ出力は呼び出し側から渡す関数に委譲することで、Bukkit/Velocity いずれのロガーにも対応する。
 */
object DatabaseInitializer {

    /** Kerria が管理する全テーブル */
    val ALL_TABLES = arrayOf(
        AccountTable, CurrencyTable, AccountBalanceTable, TransactionLogTable, ExchangeRateTable, BankMemberTable,
    )

    /**
     * 接続結果
     *
     * @property success 接続に成功したか
     * @property dataSource PostgreSQL 使用時の接続プール（SQLite・失敗時は null）
     */
    data class ConnectResult(val success: Boolean, val dataSource: HikariDataSource?)

    /**
     * データベースへ接続する
     *
     * SQLite は直接接続、PostgreSQL は HikariCP 接続プールを使用する。
     *
     * @param config データベース設定
     * @param dataDir SQLite ファイルを配置するディレクトリ
     * @param log ログ出力関数
     */
    fun connect(config: DatabaseConfig, dataDir: File, log: (String) -> Unit): ConnectResult {
        return when (config.mode) {
            "sqlite" -> {
                Database.connect(
                    "jdbc:sqlite:${dataDir.resolve("${config.database}.db").absolutePath}",
                    "org.sqlite.JDBC",
                )
                log("SQLite database connected!")
                ConnectResult(true, null)
            }
            "postgresql" -> {
                val pool = config.pool
                val hikariConfig = HikariConfig().apply {
                    jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
                    driverClassName = "org.postgresql.Driver"
                    username = config.username
                    password = config.password
                    maximumPoolSize = pool.maximumPoolSize
                    minimumIdle = pool.minimumIdle
                    connectionTimeout = pool.connectionTimeout
                    idleTimeout = pool.idleTimeout
                    maxLifetime = pool.maxLifetime
                    poolName = "kerria-pool"
                }
                val ds = HikariDataSource(hikariConfig)
                Database.connect(ds)
                log("PostgreSQL database connected with HikariCP! (pool: ${pool.maximumPoolSize} max, ${pool.minimumIdle} idle)")
                ConnectResult(true, ds)
            }
            else -> {
                log("Invalid database type: ${config.mode}. Plugin will not function correctly.")
                ConnectResult(false, null)
            }
        }
    }

    /**
     * 全テーブルを作成し、既存テーブルに不足カラムがあれば追加する
     */
    fun createTables() {
        transaction {
            SchemaUtils.create(*ALL_TABLES)
            val migrationStatements = MigrationUtils.statementsRequiredForDatabaseMigration(*ALL_TABLES, withLogs = true)
            migrationStatements.forEach { exec(it) }
        }
    }

    /**
     * デフォルト通貨がなければ設定から作成する（通貨が1つでも存在すればスキップ）
     */
    fun seedDefaultCurrency(currencyConfig: CurrencyConfig, log: (String) -> Unit) {
        transaction {
            if (CurrencyTable.selectAll().count() > 0) return@transaction
            val generatedId = CurrencyTable.insertAndGetId {
                it[name] = currencyConfig.name
                it[symbol] = currencyConfig.symbol
                it[plural] = currencyConfig.plural
                it[format] = currencyConfig.format
                it[fractionalDigits] = currencyConfig.fractionalDigits
                it[thousandsSeparator] = currencyConfig.thousandsSeparator
                it[decimalSeparator] = currencyConfig.decimalSeparator
            }
            log("Default currency '${currencyConfig.name}' created with id=${generatedId.value}.")
        }
    }
}
