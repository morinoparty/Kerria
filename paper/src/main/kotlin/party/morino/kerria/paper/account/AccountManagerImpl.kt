package party.morino.kerria.paper.account

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.paper.database.repository.AccountRepository
import java.math.BigDecimal
import java.util.UUID

/**
 * アカウント管理機能の実装クラス
 *
 * Repository を通じてDB操作を行い、API 層の data class を返す。
 * 全ての操作でDB例外をKerriaErrorに変換して返す。
 */
class AccountManagerImpl : AccountManager, KoinComponent {
    private val accountRepository: AccountRepository by inject()

    override fun getAccount(playerUniqueId: UUID): Either<KerriaError, Account> = runCatching {
        transaction {
            accountRepository.findByPlayerUniqueId(playerUniqueId)?.right()
                ?: KerriaError.AccountNotFound(playerUniqueId.toString()).left()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to get account: ${e.message}", e).left()
    }

    override fun getOrCreateAccount(playerUniqueId: UUID, playerName: String): Either<KerriaError, Account> =
        runCatching {
            transaction {
                // 既存アカウントがあればそのまま返す
                val existing = accountRepository.findByPlayerUniqueId(playerUniqueId)
                if (existing != null) {
                    return@transaction existing.right()
                }
                // 新規作成を試みる（競合時は制約例外が発生する）
                try {
                    accountRepository.create(playerUniqueId, playerName).right()
                } catch (_: Exception) {
                    // 制約例外 = 並行で作成済み → 再取得する
                    accountRepository.findByPlayerUniqueId(playerUniqueId)?.right()
                        ?: KerriaError.AccountNotFound(playerUniqueId.toString()).left()
                }
            }
        }.getOrElse { e ->
            KerriaError.DatabaseError("Failed to create account: ${e.message}", e).left()
        }

    override fun getOrCreateServiceAccount(serviceName: String): Either<KerriaError, Account> =
        runCatching {
            transaction {
                // 既存サービスアカウントがあればそのまま返す
                val existing = accountRepository.findByServiceName(serviceName)
                if (existing != null) {
                    return@transaction existing.right()
                }
                // 新規作成を試みる（競合時は制約例外が発生する）
                try {
                    accountRepository.createServiceAccount(serviceName).right()
                } catch (_: Exception) {
                    // 制約例外 = 並行で作成済み → 再取得する
                    accountRepository.findByServiceName(serviceName)?.right()
                        ?: KerriaError.AccountNotFound(serviceName).left()
                }
            }
        }.getOrElse { e ->
            KerriaError.DatabaseError("Failed to create service account: ${e.message}", e).left()
        }

    override fun getBalance(accountId: UUID, currencyId: Int): Either<KerriaError, BigDecimal> = runCatching {
        transaction {
            // アカウントが存在するか確認
            accountRepository.findById(accountId)
                ?: return@transaction KerriaError.AccountNotFound(accountId.toString()).left()

            accountRepository.getBalance(accountId, currencyId).right()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to get balance: ${e.message}", e).left()
    }
}
