package party.morino.kerria.api.files

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

/**
 * プラグインの設定を表現するデータクラス
 *
 * @property debug デバッグモードかどうか
 * @property database データベースの設定
 */
@Serializable
data class Config(
    @YamlComment("Comment")
    val debug: Boolean = false,
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
    val password : String = "password"
)


@Serializable
data class EconomyConfig(
    @YamlComment("小数点以下の桁数")
    val fractionalDigits : Int = 2,
    val currency : CurrencyConfig = CurrencyConfig()
)

@Serializable
data class CurrencyConfig(
    @YamlComment("通貨ID")
    val id : Int = 1,
    @YamlComment("通貨名")
    val name : String = "JPY",
    @YamlComment("通貨記号")
    val symbol : String = "¥",
    @YamlComment("通貨複数形")
    val plural : String = "円",
    @YamlComment("通貨フォーマット")
    val format : String = "%amount% %plural%",
    @YamlComment("小数点以下の桁数")
    val fractionalDigits : Int = 2
)
