package party.morino.kerria.api.files

import kotlinx.serialization.Serializable

/**
 * プラグインの設定を表現するデータクラス
 *
 * @property debug デバッグモードかどうか
 * @property defaultLocale 既定の言語ロケール
 * @property economy 経済機能の設定
 * @property database データベースの設定
 */
@Serializable
data class Config(
    val debug: Boolean = false,
    val defaultLocale: String = "ja",
    val economy: EconomyConfig = EconomyConfig(),
    val database: DatabaseConfig = DatabaseConfig(),
)

/**
 * データベースの設定を表現するデータクラス
 *
 * @property mode データベースの種類(sqlite, postgresql)
 * @property host データベースのホスト
 * @property port データベースのポート
 * @property database データベース名
 * @property username データベースのユーザー名
 * @property password データベースのパスワード
 */

@Serializable
data class DatabaseConfig(
    val mode : String = "sqlite",
    val host : String = "localhost",
    val port : Int = 5432,
    val database : String = "kerria",
    val username : String = "user",
    val password : String = "password",
    val pool : ConnectionPoolConfig = ConnectionPoolConfig()
)

/**
 * HikariCP接続プールの設定（PostgreSQL使用時に有効）
 *
 * @property maximumPoolSize 最大コネクション数
 * @property minimumIdle 最小アイドルコネクション数
 * @property connectionTimeout 接続タイムアウト（ミリ秒）
 * @property idleTimeout アイドルタイムアウト（ミリ秒）
 * @property maxLifetime コネクションの最大生存時間（ミリ秒）
 */
@Serializable
data class ConnectionPoolConfig(
    val maximumPoolSize : Int = 10,
    val minimumIdle : Int = 2,
    val connectionTimeout : Long = 30000,
    val idleTimeout : Long = 600000,
    val maxLifetime : Long = 1800000,
)


/**
 * 経済機能の設定を表現するデータクラス
 *
 * 通貨そのもの（名前・記号・複数形・フォーマットなど）はデータベースの通貨テーブルで管理し、
 * 設定ファイルにはサーバーごとに変えたい値だけを持たせる。
 *
 * @property initialBalance 新規プレイヤーの初期残高（デフォルト通貨）
 * @property defaultCurrencyId デフォルト通貨のID（通貨テーブルのID）
 */
@Serializable
data class EconomyConfig(
    val initialBalance : Double = 0.0,
    val defaultCurrencyId : Int = 1,
)
