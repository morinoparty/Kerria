package party.morino.kerria.paper.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

/**
 * アカウントテーブルの定義
 *
 * プレイヤーアカウントとサービスアカウントの両方を格納するテーブル。
 * 残高は AccountBalanceTable で通貨ごとに管理する。
 */
object AccountTable : UUIDTable("accounts") {
    // アカウント種別（PLAYER / SERVER / PLUGIN / SYSTEM）
    val accountType = varchar("account_type", 16).default("PLAYER")
    // プレイヤーのUUID（PLAYERの場合のみ、一意）
    val playerUniqueId = varchar("player_unique_id", 36).nullable().uniqueIndex()
    // アカウント名（プレイヤー名またはサービス名）
    val name = varchar("name", 128).nullable()
}
