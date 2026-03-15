package party.morino.kerria.paper.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

/**
 * アカウントテーブルの定義
 *
 * プレイヤーのアカウント情報を格納するテーブル。
 * 残高は AccountBalanceTable で通貨ごとに管理する。
 */
object AccountTable : UUIDTable("accounts") {
    // プレイヤーのUUID（一意）
    val playerUniqueId = varchar("player_unique_id", 36).uniqueIndex()
    // プレイヤー名
    val playerName = varchar("player_name", 16)
}
