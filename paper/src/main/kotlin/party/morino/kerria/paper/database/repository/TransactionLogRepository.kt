package party.morino.kerria.paper.database.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import party.morino.kerria.api.log.TransactionLog
import party.morino.kerria.paper.database.table.TransactionLogTable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * 取引ログのDB操作を集約するリポジトリ
 */
class TransactionLogRepository {

    /**
     * 取引ログを記録する
     */
    fun create(
        fromAccountId: UUID,
        toAccountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String?,
        timestamp: LocalDateTime,
    ): TransactionLog {
        val id = TransactionLogTable.insertAndGetId {
            it[TransactionLogTable.fromAccountId] = fromAccountId
            it[TransactionLogTable.toAccountId] = toAccountId
            it[TransactionLogTable.currencyId] = currencyId
            it[TransactionLogTable.amount] = amount
            it[TransactionLogTable.message] = message
            it[TransactionLogTable.timestamp] = timestamp
        }
        return TransactionLog(
            id = id.value,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            currencyId = currencyId,
            amount = amount,
            message = message,
            timestamp = timestamp,
        )
    }

    /**
     * 指定アカウントの取引履歴を取得する（送金元 or 送金先のいずれか）
     */
    fun findByAccountId(accountId: UUID, limit: Int, offset: Int): List<TransactionLog> {
        return TransactionLogTable
            .selectAll()
            .where {
                (TransactionLogTable.fromAccountId eq accountId) or
                    (TransactionLogTable.toAccountId eq accountId)
            }
            .orderBy(TransactionLogTable.timestamp, SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { it.toTransactionLog() }
    }

    /**
     * ResultRow を TransactionLog data class に変換する
     */
    private fun ResultRow.toTransactionLog(): TransactionLog {
        return TransactionLog(
            id = this[TransactionLogTable.id].value,
            fromAccountId = this[TransactionLogTable.fromAccountId].value,
            toAccountId = this[TransactionLogTable.toAccountId].value,
            currencyId = this[TransactionLogTable.currencyId].value,
            amount = this[TransactionLogTable.amount],
            message = this[TransactionLogTable.message],
            timestamp = this[TransactionLogTable.timestamp],
        )
    }
}
