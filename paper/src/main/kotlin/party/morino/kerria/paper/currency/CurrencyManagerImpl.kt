package party.morino.kerria.paper.currency

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.paper.database.repository.CurrencyRepository

/**
 * 通貨管理機能の実装クラス
 *
 * DB例外をKerriaErrorに変換して返す。
 */
class CurrencyManagerImpl : CurrencyManager, KoinComponent {
    private val currencyRepository: CurrencyRepository by inject()
    private val configManager: ConfigManager by inject()

    override fun getCurrency(id: Int): Either<KerriaError, Currency> = runCatching {
        transaction {
            currencyRepository.findById(id)?.right()
                ?: KerriaError.CurrencyNotFound(id.toString()).left()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to get currency: ${e.message}", e).left()
    }

    override fun getDefaultCurrency(): Either<KerriaError, Currency> {
        val defaultId = configManager.getConfig().economy.currency.id
        return getCurrency(defaultId)
    }

    override fun createCurrency(
        name: String,
        symbol: String,
        format: String,
        decimals: Int,
        plural: String,
    ): Either<KerriaError, Currency> = runCatching {
        transaction {
            currencyRepository.create(name, symbol, format, decimals, plural).right()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to create currency: ${e.message}", e).left()
    }

    override fun getAllCurrencies(): Either<KerriaError, List<Currency>> = runCatching {
        transaction {
            currencyRepository.findAll().right()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to get currencies: ${e.message}", e).left()
    }

    override fun getCurrencyByName(name: String): Either<KerriaError, Currency> = runCatching {
        transaction {
            currencyRepository.findByName(name)?.right()
                ?: KerriaError.CurrencyNotFound(name).left()
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to get currency: ${e.message}", e).left()
    }

    override fun deleteCurrency(id: Int): Either<KerriaError, Unit> = runCatching {
        transaction {
            // デフォルト通貨の削除を防止
            val defaultId = configManager.getConfig().economy.currency.id
            if (id == defaultId) {
                return@transaction KerriaError.InvalidAmount(
                    java.math.BigDecimal.ZERO,
                    "Cannot delete default currency",
                ).left()
            }

            val deleted = currencyRepository.deleteById(id)
            if (deleted == 0) {
                KerriaError.CurrencyNotFound(id.toString()).left()
            } else {
                Unit.right()
            }
        }
    }.getOrElse { e ->
        KerriaError.DatabaseError("Failed to delete currency: ${e.message}", e).left()
    }
}
