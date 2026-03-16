package party.morino.kerria.api.account

import java.util.UUID

/**
 * アカウント情報を表現する不変スナップショット
 *
 * プレイヤーアカウントとサービスアカウントの両方を表現する。
 * プレイヤーアカウントの場合は playerUniqueId が設定される。
 *
 * @property accountId アカウント固有ID (UUID v7)
 * @property accountType アカウントの種類
 * @property playerUniqueId プレイヤーUUID (PLAYERの場合のみ)
 * @property name アカウント名（プレイヤー名またはサービス名）
 */
data class Account(
    val accountId: UUID,
    val accountType: AccountType,
    val playerUniqueId: UUID?,
    val name: String?,
)
