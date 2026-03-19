package party.morino.kerria.paper.database.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import party.morino.kerria.paper.database.table.ExchangeRateTable
import java.math.BigDecimal

/**
 * 為替レートのDB操作を集約するリポジトリ
 */
class ExchangeRateRepository {

    /**
     * 通貨ペアの為替レートを取得する
     */
    fun findRate(fromCurrencyId: Int, toCurrencyId: Int): BigDecimal? {
        return ExchangeRateTable
            .selectAll()
            .where {
                (ExchangeRateTable.fromCurrencyId eq fromCurrencyId) and
                    (ExchangeRateTable.toCurrencyId eq toCurrencyId)
            }
            .map { it[ExchangeRateTable.rate] }
            .firstOrNull()
    }

    /**
     * 為替レートを設定する（既に存在する場合は更新）
     */
    fun setRate(fromCurrencyId: Int, toCurrencyId: Int, rate: BigDecimal) {
        ExchangeRateTable.upsert(
            ExchangeRateTable.fromCurrencyId,
            ExchangeRateTable.toCurrencyId,
        ) {
            it[ExchangeRateTable.fromCurrencyId] = fromCurrencyId
            it[ExchangeRateTable.toCurrencyId] = toCurrencyId
            it[ExchangeRateTable.rate] = rate
        }
    }

    /**
     * 為替レートを削除する
     */
    fun deleteRate(fromCurrencyId: Int, toCurrencyId: Int): Int {
        return ExchangeRateTable.deleteWhere {
            (ExchangeRateTable.fromCurrencyId eq fromCurrencyId) and
                (ExchangeRateTable.toCurrencyId eq toCurrencyId)
        }
    }

    /**
     * 全ての為替レートを取得する
     */
    fun findAll(): List<Triple<Int, Int, BigDecimal>> {
        return ExchangeRateTable
            .selectAll()
            .map {
                Triple(
                    it[ExchangeRateTable.fromCurrencyId].value,
                    it[ExchangeRateTable.toCurrencyId].value,
                    it[ExchangeRateTable.rate],
                )
            }
    }
}
