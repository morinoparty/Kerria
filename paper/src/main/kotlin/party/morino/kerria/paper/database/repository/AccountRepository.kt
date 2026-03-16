package party.morino.kerria.paper.database.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.minus
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.account.AccountType
import party.morino.kerria.paper.database.table.AccountBalanceTable
import party.morino.kerria.paper.database.table.AccountTable
import java.math.BigDecimal
import java.util.UUID

/**
 * アカウントのDB操作を集約するリポジトリ
 */
class AccountRepository {

    /**
     * プレイヤーUUIDからアカウントを検索する
     */
    fun findByPlayerUniqueId(uuid: UUID): Account? {
        return AccountTable
            .selectAll()
            .where { AccountTable.playerUniqueId eq uuid.toString() }
            .map { it.toAccount() }
            .firstOrNull()
    }

    /**
     * サービス名からサービスアカウントを検索する
     */
    fun findByServiceName(serviceName: String): Account? {
        return AccountTable
            .selectAll()
            .where { AccountTable.serviceName eq serviceName }
            .map { it.toAccount() }
            .firstOrNull()
    }

    /**
     * アカウントIDからアカウントを検索する
     */
    fun findById(accountId: UUID): Account? {
        return AccountTable
            .selectAll()
            .where { AccountTable.id eq accountId }
            .map { it.toAccount() }
            .firstOrNull()
    }

    /**
     * 新しいプレイヤーアカウントを作成する
     */
    fun create(playerUniqueId: UUID, playerName: String): Account {
        val id = AccountTable.insertAndGetId {
            it[AccountTable.accountType] = AccountType.PLAYER.name
            it[AccountTable.playerUniqueId] = playerUniqueId.toString()
            it[AccountTable.playerName] = playerName
        }
        return Account(
            accountId = id.value,
            accountType = AccountType.PLAYER,
            playerUniqueId = playerUniqueId,
            playerName = playerName,
            serviceName = null,
        )
    }

    /**
     * 新しいサービスアカウントを作成する
     */
    fun createServiceAccount(serviceName: String, accountType: AccountType): Account {
        val id = AccountTable.insertAndGetId {
            it[AccountTable.accountType] = accountType.name
            it[AccountTable.serviceName] = serviceName
        }
        return Account(
            accountId = id.value,
            accountType = accountType,
            playerUniqueId = null,
            playerName = null,
            serviceName = serviceName,
        )
    }

    /**
     * 指定アカウント・通貨の残高を取得する（未登録なら0）
     */
    fun getBalance(accountId: UUID, currencyId: Int): BigDecimal {
        return AccountBalanceTable
            .selectAll()
            .where {
                (AccountBalanceTable.accountId eq accountId) and
                    (AccountBalanceTable.currencyId eq currencyId)
            }
            .map { it[AccountBalanceTable.balance] }
            .firstOrNull() ?: BigDecimal.ZERO
    }

    /**
     * 残高行が存在しなければ初期値0で作成する（既にあれば何もしない）
     */
    fun ensureBalanceRow(accountId: UUID, currencyId: Int) {
        AccountBalanceTable.insertIgnore {
            it[AccountBalanceTable.accountId] = accountId
            it[AccountBalanceTable.currencyId] = currencyId
            it[AccountBalanceTable.balance] = BigDecimal.ZERO
        }
    }

    /**
     * 残高をアトミックに加算する（balance = balance + delta）
     *
     * @return 更新された行数（0の場合は行が存在しない）
     */
    fun addBalance(accountId: UUID, currencyId: Int, delta: BigDecimal): Int {
        return AccountBalanceTable.update(
            where = {
                (AccountBalanceTable.accountId eq accountId) and
                    (AccountBalanceTable.currencyId eq currencyId)
            },
        ) {
            it.update(AccountBalanceTable.balance, AccountBalanceTable.balance plus delta)
        }
    }

    /**
     * 残高をアトミックに減算する（残高チェック付き）
     *
     * balance >= amount の場合のみ balance = balance - amount を実行する。
     * @return 更新された行数（0の場合は残高不足または行が存在しない）
     */
    fun subtractBalance(accountId: UUID, currencyId: Int, amount: BigDecimal): Int {
        return AccountBalanceTable.update(
            where = {
                (AccountBalanceTable.accountId eq accountId) and
                    (AccountBalanceTable.currencyId eq currencyId) and
                    (AccountBalanceTable.balance greaterEq amount)
            },
        ) {
            it.update(AccountBalanceTable.balance, AccountBalanceTable.balance minus amount)
        }
    }

    /**
     * ResultRow を Account data class に変換する
     */
    private fun ResultRow.toAccount(): Account {
        val type = AccountType.valueOf(this[AccountTable.accountType])
        val playerUidStr = this[AccountTable.playerUniqueId]
        return Account(
            accountId = this[AccountTable.id].value,
            accountType = type,
            playerUniqueId = playerUidStr?.let { UUID.fromString(it) },
            playerName = this[AccountTable.playerName],
            serviceName = this[AccountTable.serviceName],
        )
    }
}
