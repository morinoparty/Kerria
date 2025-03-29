package party.morino.kerria.currency

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.error.KerriaError.CurrencyNotFound

/**
 * CurrencyManagerの実装
 */
class CurrencyManagerImpl : CurrencyManager {
    override suspend fun getCurrency(id: Int): Either<KerriaError, Currency> {
        return newSuspendedTransaction {
            CurrencyEntity.findById(id)?.right() ?: CurrencyNotFound("Currency not found: $id").left()
        }
    }

    override suspend fun createCurrency(
        name: String,
        symbol: String,
        format: String,
        decimals: Int,
        plural: String
    ): Either<KerriaError, Currency> {
        return newSuspendedTransaction {
            CurrencyEntity.new {
                this.name = name
                this.symbol = symbol
                this.format = format
                this.fractionalDigits = decimals
                this.plural = plural
            }.right()
        }
    }

    override suspend fun getAllCurrencies(): Either<KerriaError, List<Currency>> {
        return newSuspendedTransaction {
            CurrencyEntity.all().toList().right()
        }
    }
} 