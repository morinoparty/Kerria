package party.morino.kerria.api.economy

import java.math.BigDecimal

/**
 * 通貨ペアの為替レートを表す不変データクラス
 *
 * `fromCurrency * rate = toCurrency` の関係を表す。レートは方向ごとに保持される。
 *
 * @property fromCurrencyId 変換元通貨ID
 * @property toCurrencyId 変換先通貨ID
 * @property rate 為替レート
 */
data class ExchangeRate(
    val fromCurrencyId: Int,
    val toCurrencyId: Int,
    val rate: BigDecimal,
)
