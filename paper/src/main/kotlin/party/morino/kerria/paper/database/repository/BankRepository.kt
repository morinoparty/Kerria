package party.morino.kerria.paper.database.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import party.morino.kerria.api.account.AccountType
import party.morino.kerria.api.account.Bank
import party.morino.kerria.paper.database.table.AccountBalanceTable
import party.morino.kerria.paper.database.table.AccountTable
import party.morino.kerria.paper.database.table.BankMemberTable
import java.util.UUID

/**
 * 銀行アカウントとメンバーシップのDB操作を集約するリポジトリ
 */
class BankRepository {

    /**
     * 銀行名から銀行を検索する
     */
    fun findByName(name: String): Bank? {
        val row = AccountTable
            .selectAll()
            .where { (AccountTable.name eq name) and (AccountTable.accountType eq AccountType.BANK.name) }
            .firstOrNull() ?: return null
        return row.toBank()
    }

    /**
     * すべての銀行を取得する
     */
    fun findAll(): List<Bank> {
        return AccountTable
            .selectAll()
            .where { AccountTable.accountType eq AccountType.BANK.name }
            .map { it.toBank() }
    }

    /**
     * 新しい銀行を作成し、所有者をメンバーとして登録する
     */
    fun create(name: String, ownerUuid: UUID): Bank {
        val id = AccountTable.insertAndGetId {
            it[AccountTable.accountType] = AccountType.BANK.name
            it[AccountTable.name] = name
        }
        BankMemberTable.insert {
            it[BankMemberTable.bankAccountId] = id.value
            it[BankMemberTable.memberUniqueId] = ownerUuid.toString()
            it[BankMemberTable.role] = ROLE_OWNER
        }
        return Bank(accountId = id.value, name = name, ownerUuid = ownerUuid)
    }

    /**
     * 銀行を削除する（メンバー・残高・アカウント行をまとめて削除）
     */
    fun delete(accountId: UUID) {
        BankMemberTable.deleteWhere { BankMemberTable.bankAccountId eq accountId }
        AccountBalanceTable.deleteWhere { AccountBalanceTable.accountId eq accountId }
        AccountTable.deleteWhere { AccountTable.id eq accountId }
    }

    /**
     * 指定プレイヤーが銀行のメンバー（役割を問わない）かどうかを返す
     */
    fun isMember(accountId: UUID, uuid: UUID): Boolean {
        return BankMemberTable
            .selectAll()
            .where {
                (BankMemberTable.bankAccountId eq accountId) and
                    (BankMemberTable.memberUniqueId eq uuid.toString())
            }
            .any()
    }

    /**
     * 指定プレイヤーが銀行の所有者かどうかを返す
     */
    fun isOwner(accountId: UUID, uuid: UUID): Boolean {
        return BankMemberTable
            .selectAll()
            .where {
                (BankMemberTable.bankAccountId eq accountId) and
                    (BankMemberTable.memberUniqueId eq uuid.toString()) and
                    (BankMemberTable.role eq ROLE_OWNER)
            }
            .any()
    }

    /**
     * 銀行にメンバーを追加する（既に存在する場合は何もしない）
     */
    fun addMember(accountId: UUID, uuid: UUID) {
        BankMemberTable.insertIgnore {
            it[BankMemberTable.bankAccountId] = accountId
            it[BankMemberTable.memberUniqueId] = uuid.toString()
            it[BankMemberTable.role] = ROLE_MEMBER
        }
    }

    /**
     * 銀行からメンバーを削除する（所有者は削除しない）
     *
     * @return 削除された行数
     */
    fun removeMember(accountId: UUID, uuid: UUID): Int {
        return BankMemberTable.deleteWhere {
            (BankMemberTable.bankAccountId eq accountId) and
                (BankMemberTable.memberUniqueId eq uuid.toString()) and
                (BankMemberTable.role neq ROLE_OWNER)
        }
    }

    /**
     * 銀行アカウント行を Bank data class に変換する（所有者を付随して取得）
     */
    private fun ResultRow.toBank(): Bank {
        val accountId = this[AccountTable.id].value
        val owner = BankMemberTable
            .selectAll()
            .where {
                (BankMemberTable.bankAccountId eq accountId) and
                    (BankMemberTable.role eq ROLE_OWNER)
            }
            .map { it[BankMemberTable.memberUniqueId] }
            .firstOrNull()
        return Bank(
            accountId = accountId,
            name = this[AccountTable.name] ?: "",
            ownerUuid = owner?.let { UUID.fromString(it) },
        )
    }

    companion object {
        private const val ROLE_OWNER = "OWNER"
        private const val ROLE_MEMBER = "MEMBER"
    }
}
