package party.morino.kerria.paper.currency

import arrow.core.Either
import arrow.core.right
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.paper.model.database.CurrencyTable
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 通貨のエンティティ
 */
class CurrencyEntity(id: EntityID<Int>) : IntEntity(id), Currency {
    companion object : IntEntityClass<CurrencyEntity>(CurrencyTable)

    override var name by CurrencyTable.name
    override var symbol by CurrencyTable.symbol
    override var plural by CurrencyTable.plural
    override var format by CurrencyTable.format
    override var fractionalDigits by CurrencyTable.fractionalDigits

    override val currencyId: Int
        get() = super.id.value

    override suspend fun format(amount: BigDecimal): Either<KerriaError, String> {
        return format
            .replace("%amount%", round(amount).map { it.toPlainString() }.getOrNull() ?: "")
            .replace("%plural%", plural)
            .right()
    }

    override suspend fun round(amount: BigDecimal): Either<KerriaError, BigDecimal> {
        return amount.setScale(fractionalDigits, RoundingMode.HALF_UP).right()
    }
}
