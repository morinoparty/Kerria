package party.morino.kerria.paper.currency

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.currency.CurrencyFormatter
import java.math.BigDecimal
import kotlin.test.assertEquals

/**
 * 桁区切り整形ユーティリティと通貨フォーマットの単体テスト
 *
 * 外部依存の無い純粋ロジックのため Koin 等は不要。主要な分岐のみを検証する。
 */
class CurrencyFormatterTest {

    @Test
    @DisplayName("Group integer part every three digits")
    fun groupsThousands() {
        val result = CurrencyFormatter.format(BigDecimal("1234567.89"), 2, ",", ".")
        assertEquals("1,234,567.89", result)
    }

    @Test
    @DisplayName("No decimal digits omits decimal separator")
    fun zeroFractionalDigits() {
        val result = CurrencyFormatter.format(BigDecimal("1234"), 0, ",", ".")
        assertEquals("1,234", result)
    }

    @Test
    @DisplayName("Custom separators produce European style")
    fun europeanSeparators() {
        val result = CurrencyFormatter.format(BigDecimal("1234.56"), 2, ".", ",")
        assertEquals("1.234,56", result)
    }

    @Test
    @DisplayName("Empty thousands separator disables grouping")
    fun noGrouping() {
        val result = CurrencyFormatter.format(BigDecimal("1234567"), 0, "", ".")
        assertEquals("1234567", result)
    }

    @Test
    @DisplayName("Negative amount keeps sign")
    fun negativeAmount() {
        val result = CurrencyFormatter.format(BigDecimal("-1234.5"), 2, ",", ".")
        assertEquals("-1,234.50", result)
    }

    @Test
    @DisplayName("Rounds to fractional digits with HALF_UP")
    fun roundsHalfUp() {
        val result = CurrencyFormatter.format(BigDecimal("1.005"), 2, ",", ".")
        assertEquals("1.01", result)
    }

    @Test
    @DisplayName("Currency format replaces symbol placeholder as prefix")
    fun currencyFormatSymbolPrefix() {
        val usd = Currency(
            id = 2, name = "USD", plural = "Dollars", symbol = "$",
            format = "%symbol%%amount%", fractionalDigits = 2,
        )
        assertEquals("$1,234.56", usd.format(BigDecimal("1234.56")))
    }

    @Test
    @DisplayName("Currency format supports suffix symbol with custom separators")
    fun currencyFormatSymbolSuffix() {
        val eur = Currency(
            id = 3, name = "EUR", plural = "Euros", symbol = "€",
            format = "%amount%%symbol%", fractionalDigits = 2,
            thousandsSeparator = ".", decimalSeparator = ",",
        )
        assertEquals("1.234,56€", eur.format(BigDecimal("1234.56")))
    }

    @Test
    @DisplayName("Default plural format reflects grouping")
    fun currencyFormatDefaultPlural() {
        val jpy = Currency(
            id = 1, name = "JPY", plural = "円", symbol = "¥",
            format = "%amount% %plural%", fractionalDigits = 0,
        )
        assertEquals("1,234 円", jpy.format(BigDecimal("1234")))
    }
}
