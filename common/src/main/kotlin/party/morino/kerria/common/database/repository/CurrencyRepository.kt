package party.morino.kerria.common.database.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.common.database.table.CurrencyTable

/**
 * 通貨のDB操作を集約するリポジトリ
 */
class CurrencyRepository {

    /**
     * IDから通貨を検索する
     */
    fun findById(id: Int): Currency? {
        return CurrencyTable
            .selectAll()
            .where { CurrencyTable.id eq id }
            .map { it.toCurrency() }
            .firstOrNull()
    }

    /**
     * 新しい通貨を作成する
     */
    fun create(
        name: String,
        symbol: String,
        format: String,
        decimals: Int,
        plural: String,
        thousandsSeparator: String = ",",
        decimalSeparator: String = ".",
    ): Currency {
        val id = CurrencyTable.insertAndGetId {
            it[CurrencyTable.name] = name
            it[CurrencyTable.symbol] = symbol
            it[CurrencyTable.format] = format
            it[CurrencyTable.fractionalDigits] = decimals
            it[CurrencyTable.plural] = plural
            it[CurrencyTable.thousandsSeparator] = thousandsSeparator
            it[CurrencyTable.decimalSeparator] = decimalSeparator
        }
        return Currency(
            id = id.value,
            name = name,
            plural = plural,
            symbol = symbol,
            format = format,
            fractionalDigits = decimals,
            thousandsSeparator = thousandsSeparator,
            decimalSeparator = decimalSeparator,
        )
    }

    /**
     * 通貨名から通貨を検索する
     */
    fun findByName(name: String): Currency? {
        return CurrencyTable
            .selectAll()
            .where { CurrencyTable.name eq name }
            .map { it.toCurrency() }
            .firstOrNull()
    }

    /**
     * 全ての通貨を取得する
     */
    fun findAll(): List<Currency> {
        return CurrencyTable
            .selectAll()
            .map { it.toCurrency() }
    }

    /**
     * 既存の通貨を更新する
     *
     * `currency.id` で対象を特定し、全フィールドを上書きする。
     *
     * @return 更新された行数
     */
    fun update(currency: Currency): Int {
        return CurrencyTable.update({ CurrencyTable.id eq currency.id }) {
            it[name] = currency.name
            it[plural] = currency.plural
            it[symbol] = currency.symbol
            it[format] = currency.format
            it[fractionalDigits] = currency.fractionalDigits
            it[thousandsSeparator] = currency.thousandsSeparator
            it[decimalSeparator] = currency.decimalSeparator
        }
    }

    /**
     * 通貨を削除する
     *
     * @return 削除された行数
     */
    fun deleteById(id: Int): Int {
        return CurrencyTable.deleteWhere { CurrencyTable.id eq id }
    }

    /**
     * ResultRow を Currency data class に変換する
     */
    private fun ResultRow.toCurrency(): Currency {
        return Currency(
            id = this[CurrencyTable.id].value,
            name = this[CurrencyTable.name],
            plural = this[CurrencyTable.plural],
            symbol = this[CurrencyTable.symbol],
            format = this[CurrencyTable.format],
            fractionalDigits = this[CurrencyTable.fractionalDigits],
            thousandsSeparator = this[CurrencyTable.thousandsSeparator],
            decimalSeparator = this[CurrencyTable.decimalSeparator],
        )
    }
}
