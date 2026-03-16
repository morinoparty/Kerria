package party.morino.kerria.paper.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

/**
 * アカウントテーブルの定義
 *
 * プレイヤーアカウントとサービスアカウントの両方を格納するテーブル。
 * 残高は AccountBalanceTable で通貨ごとに管理する。
 */
object AccountTable : UUIDTable("accounts") {
    // アカウント種別（PLAYER / SERVICE）
    val accountType = varchar("account_type", 16).default("PLAYER")
    // プレイヤーのUUID（PLAYERの場合のみ、一意）
    val playerUniqueId = varchar("player_unique_id", 36).nullable().uniqueIndex()
    // プレイヤー名（PLAYERの場合のみ）
    val playerName = varchar("player_name", 16).nullable()
    // サービス名（SERVICEの場合のみ、一意）
    val serviceName = varchar("service_name", 128).nullable().uniqueIndex()
}
