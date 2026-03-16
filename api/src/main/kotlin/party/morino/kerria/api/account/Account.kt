package party.morino.kerria.api.account

import java.util.UUID

/**
 * アカウント情報を表現する不変スナップショット
 *
 * プレイヤーアカウントとサービスアカウントの両方を表現する。
 * プレイヤーアカウントの場合は playerUniqueId と playerName が設定される。
 * 非PLAYERアカウントの場合は serviceName が設定される。
 *
 * @property accountId アカウント固有ID (UUID v7)
 * @property accountType アカウントの種類
 * @property playerUniqueId プレイヤーUUID (PLAYERの場合のみ)
 * @property playerName プレイヤー名 (PLAYERの場合のみ)
 * @property serviceName サービス名 (非PLAYERの場合のみ、例: "shop_plugin")
 */
data class Account(
    val accountId: UUID,
    val accountType: AccountType,
    val playerUniqueId: UUID?,
    val playerName: String?,
    val serviceName: String?,
)
