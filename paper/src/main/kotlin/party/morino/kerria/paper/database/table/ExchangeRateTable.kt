package party.morino.kerria.paper.database.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

/**
 * 為替レートテーブルの定義
 *
 * 通貨ペア間の交換レートを管理する。
 */
object ExchangeRateTable : LongIdTable("exchange_rates") {
    // 変換元通貨への外部キー
    val fromCurrencyId = reference("from_currency_id", CurrencyTable)
    // 変換先通貨への外部キー
    val toCurrencyId = reference("to_currency_id", CurrencyTable)
    // 為替レート（fromCurrency * rate = toCurrency）
    val rate = decimal("rate", precision = 20, scale = 8)

    init {
        // 同じ通貨ペアのレートは1つだけ
        uniqueIndex(fromCurrencyId, toCurrencyId)
    }
}
