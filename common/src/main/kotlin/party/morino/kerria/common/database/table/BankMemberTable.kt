package party.morino.kerria.common.database.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

/**
 * 銀行メンバーシップテーブルの定義
 *
 * 銀行アカウントに所属するプレイヤー（所有者・メンバー）を管理する。
 * (bankAccountId, memberUniqueId) の組み合わせで一意。
 */
object BankMemberTable : LongIdTable("bank_members") {
    // 銀行アカウント（AccountType.BANK）への外部キー
    val bankAccountId = reference("bank_account_id", AccountTable)
    // メンバーのプレイヤーUUID
    val memberUniqueId = varchar("member_unique_id", 36)
    // 役割（OWNER / MEMBER）
    val role = varchar("role", 16)

    init {
        uniqueIndex(bankAccountId, memberUniqueId)
    }
}
