package party.morino.kerria.paper.log

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.api.log.TransactionLog
import party.morino.kerria.paper.database.repository.TransactionLogRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * 取引ログ管理機能の実装クラス
 *
 * DB例外をKerriaErrorに変換して返す。
 */
class LogManagerImpl : LogManager, KoinComponent {
    private val transactionLogRepository: TransactionLogRepository by inject()

    override fun logTransaction(
        fromAccountId: UUID,
        toAccountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String?,
        timestamp: LocalDateTime,
    ): Either<KerriaError, Unit> = runCatching {
        transaction {
            transactionLogRepository.create(fromAccountId, toAccountId, currencyId, amount, message, timestamp)
            Unit.right()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to log transaction: ${e.message}", e).left()
    }

    override fun getTransactionHistory(
        accountId: UUID,
        limit: Int,
        offset: Int,
    ): Either<KerriaError, List<TransactionLog>> = runCatching {
        transaction {
            transactionLogRepository.findByAccountId(accountId, limit, offset).right()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to get transaction history: ${e.message}", e).left()
    }
}
