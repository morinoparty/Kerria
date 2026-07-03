package party.morino.kerria.paper.account

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.account.Bank
import party.morino.kerria.api.account.BankManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.paper.database.repository.BankRepository
import java.math.BigDecimal
import java.util.UUID

/**
 * 銀行管理機能の実装クラス
 *
 * 銀行のメタデータ（存在・所有者・メンバー）は [BankRepository] を通じて管理し、
 * 残高の入出金は [EconomyManager] に委譲する。これにより、残高操作は通常のアカウントと
 * 同じアトミックな経路（ログ記録・イベント発火を含む）を通る。
 */
class BankManagerImpl : BankManager, KoinComponent {
    private val bankRepository: BankRepository by inject()
    private val economyManager: EconomyManager by inject()
    private val accountManager: AccountManager by inject()

    override fun createBank(name: String, ownerUuid: UUID): Either<KerriaError, Bank> = dbCatching {
        transaction {
            // 同名の銀行が既に存在する場合はエラー
            if (bankRepository.findByName(name) != null) {
                return@transaction KerriaError.BankAlreadyExists(name).left()
            }
            bankRepository.create(name, ownerUuid).right()
        }
    }

    override fun deleteBank(name: String): Either<KerriaError, Unit> = dbCatching {
        transaction {
            val bank = bankRepository.findByName(name)
                ?: return@transaction KerriaError.BankNotFound(name).left()
            bankRepository.delete(bank.accountId)
            Unit.right()
        }
    }

    override fun getBank(name: String): Either<KerriaError, Bank> = dbCatching {
        transaction {
            bankRepository.findByName(name)?.right()
                ?: KerriaError.BankNotFound(name).left()
        }
    }

    override fun listBanks(): Either<KerriaError, List<Bank>> = dbCatching {
        transaction { bankRepository.findAll().right() }
    }

    override fun bankBalance(name: String, currencyId: Int): Either<KerriaError, BigDecimal> =
        getBank(name).flatMap { bank -> accountManager.getBalance(bank.accountId, currencyId) }

    override fun deposit(
        name: String,
        currencyId: Int,
        amount: BigDecimal,
        treatePluginName: String?,
    ): Either<KerriaError, BigDecimal> =
        getBank(name).flatMap { bank ->
            // 残高操作は EconomyManager に委譲する（アトミック・ログ・イベントを再利用）
            economyManager.deposit(bank.accountId, currencyId, amount, "Bank deposit", treatePluginName)
        }

    override fun withdraw(
        name: String,
        currencyId: Int,
        amount: BigDecimal,
        treatePluginName: String?,
    ): Either<KerriaError, BigDecimal> =
        getBank(name).flatMap { bank ->
            economyManager.withdraw(bank.accountId, currencyId, amount, "Bank withdraw", treatePluginName)
        }

    override fun isOwner(name: String, uuid: UUID): Either<KerriaError, Boolean> = dbCatching {
        transaction {
            val bank = bankRepository.findByName(name)
                ?: return@transaction KerriaError.BankNotFound(name).left()
            bankRepository.isOwner(bank.accountId, uuid).right()
        }
    }

    override fun isMember(name: String, uuid: UUID): Either<KerriaError, Boolean> = dbCatching {
        transaction {
            val bank = bankRepository.findByName(name)
                ?: return@transaction KerriaError.BankNotFound(name).left()
            bankRepository.isMember(bank.accountId, uuid).right()
        }
    }

    override fun addMember(name: String, uuid: UUID): Either<KerriaError, Unit> = dbCatching {
        transaction {
            val bank = bankRepository.findByName(name)
                ?: return@transaction KerriaError.BankNotFound(name).left()
            bankRepository.addMember(bank.accountId, uuid)
            Unit.right()
        }
    }

    override fun removeMember(name: String, uuid: UUID): Either<KerriaError, Unit> = dbCatching {
        transaction {
            val bank = bankRepository.findByName(name)
                ?: return@transaction KerriaError.BankNotFound(name).left()
            bankRepository.removeMember(bank.accountId, uuid)
            Unit.right()
        }
    }

    /**
     * DB 例外を [KerriaError.DatabaseError] に変換する共通ラッパー
     */
    private inline fun <T> dbCatching(block: () -> Either<KerriaError, T>): Either<KerriaError, T> =
        runCatching(block).getOrElse { e ->
            KerriaError.DatabaseError("Bank operation failed: ${e.message}", e).left()
        }
}
