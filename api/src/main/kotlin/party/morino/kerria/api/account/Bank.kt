package party.morino.kerria.api.account

import java.util.UUID

/**
 * 共有の銀行アカウントを表す不変データクラス
 *
 * 銀行は [AccountType.BANK] のアカウントとして表現され、残高は通常のアカウントと同じ
 * 残高テーブルで管理される。所有者・メンバーは別途メンバーシップとして保持される。
 *
 * @property accountId 銀行に対応するアカウントのUUID
 * @property name 銀行名（一意）
 * @property ownerUuid 所有者プレイヤーのUUID（存在しない場合は null）
 */
data class Bank(
    val accountId: UUID,
    val name: String,
    val ownerUuid: UUID?,
)
