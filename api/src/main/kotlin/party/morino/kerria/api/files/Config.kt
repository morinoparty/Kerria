package party.morino.kerria.api.files

import kotlinx.serialization.Serializable

/**
 * プラグインの設定を表現するデータクラス
 *
 * @property debug デバッグモードかどうか
 * @property database データベースの設定
 */
@Serializable
data class Config(
    val debug: Boolean = false,
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
    val password : String = "password"
)
