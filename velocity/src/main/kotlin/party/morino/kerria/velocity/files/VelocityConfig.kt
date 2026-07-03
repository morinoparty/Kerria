package party.morino.kerria.velocity.files

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable
import party.morino.kerria.api.files.DatabaseConfig

/**
 * Velocity プラグインの設定
 *
 * Paper 側と同じ共有データベース（PostgreSQL 推奨）へ接続することで、
 * サーバーをまたいだ残高の参照・送金を可能にする。
 *
 * @property database データベース接続設定（Paper と同じDBを指定する）
 * @property defaultCurrencyId 既定の通貨ID
 */
@Serializable
data class VelocityConfig(
    val database: DatabaseConfig = DatabaseConfig(mode = "postgresql"),
    @YamlComment("既定の通貨ID")
    val defaultCurrencyId: Int = 1,
)
