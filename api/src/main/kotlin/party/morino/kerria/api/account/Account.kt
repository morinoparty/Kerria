package party.morino.kerria.api.account

import java.util.UUID

/**
 * プレイヤーのアカウント情報を表現する不変スナップショット
 *
 * @property accountId アカウント固有ID (UUID v7)
 * @property playerUniqueId プレイヤーUUID (UUID v4)
 * @property playerName プレイヤー名
 */
data class Account(
    val accountId: UUID,
    val playerUniqueId: UUID,
    val playerName: String,
)
