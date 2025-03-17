package party.morino.kerria.model.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal

/**
 * アカウントテーブルの定義
 *
 * このオブジェクトはプレイヤーのアカウント情報を格納するテーブルを定義します。
 * [UUIDTable]を継承し、UUIDを主キーとして使用します。
 *
 * @property id アカウントのUUID（主キー）
 * @property playerUniqueId プレイヤーのUUID（36文字の文字列）。一意のインデックスが設定されます。
 * @property playerName プレイヤーの名前（最大16文字）
 * @property balance アカウントの残高（BigDecimal型）
 */
object AccountTable : UUIDTable("accounts") {
    val playerUniqueId: Column<String> = varchar("player_unique_id", 36).uniqueIndex()
    val playerName: Column<String> = varchar("player_name", 16)
    val balance: Column<BigDecimal> = decimal("balance", precision = 20, scale = 4)
}