package party.morino.kerria.paper.database

import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.files.DatabaseConfig
import party.morino.kerria.paper.database.table.AccountBalanceTable
import party.morino.kerria.paper.database.table.AccountTable
import party.morino.kerria.paper.database.table.CurrencyTable
import party.morino.kerria.paper.database.table.TransactionLogTable

/**
 * データベースの接続・テーブル作成を管理するクラス
 */
class DatabaseManager(private val plugin: JavaPlugin) : KoinComponent {
    private val configManager: ConfigManager by inject()

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
     * DB接続を確立する
     *
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
                Database.connect(
                    url = "jdbc:postgresql://${config.host}:${config.port}/${config.database}",
                    driver = "org.postgresql.Driver",
                    user = config.username,
                    password = config.password,
                )
                plugin.logger.info("PostgreSQL database connected!")
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
        val tables = arrayOf(AccountTable, CurrencyTable, AccountBalanceTable, TransactionLogTable)
        transaction {
            SchemaUtils.create(*tables)
            SchemaUtils.createMissingTablesAndColumns(*tables)
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
