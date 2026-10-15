package party.morino.kerria.common.database.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import party.morino.kerria.common.database.UuidV7
import java.util.UUID

/**
 * アカウントテーブルの定義
 *
 * プレイヤーアカウントとサービスアカウントの両方を格納するテーブル。
 * 残高は AccountBalanceTable で通貨ごとに管理する。
 */
object AccountTable : IdTable<UUID>("accounts") {
    // アカウントID。UUIDTable の既定（UUID v4）ではなく、作成順に並ぶ UUID v7 を採番する
    override val id: Column<EntityID<UUID>> = javaUUID("id").clientDefault { UuidV7.generate() }.entityId()
    override val primaryKey = PrimaryKey(id)

    // アカウント種別（PLAYER / SERVER / PLUGIN / SYSTEM）
    val accountType = varchar("account_type", 16).default("PLAYER")
    // プレイヤーのUUID（PLAYERの場合のみ、一意）
    val playerUniqueId = varchar("player_unique_id", 36).nullable().uniqueIndex()
    // アカウント名（プレイヤー名またはサービス名）
    val name = varchar("name", 128).nullable()
}
