package party.morino.kerria.api.currency

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 通貨を表す不変データクラス
 *
 * @property id 通貨のID
 * @property name 通貨の名前
 * @property plural 通貨の複数形
 * @property symbol 通貨の記号
 * @property format 通貨のフォーマットパターン（例: "%amount% %plural%"）
 * @property fractionalDigits 小数点以下の桁数
 * @property thousandsSeparator 3桁ごとの桁区切り文字（空文字なら桁区切りしない）
 * @property decimalSeparator 小数点記号
 */
data class Currency(
    val id: Int,
    val name: String,
    val plural: String,
    val symbol: String,
    val format: String,
    val fractionalDigits: Int,
    val thousandsSeparator: String = ",",
    val decimalSeparator: String = ".",
) {
    /**
     * 金額をこの通貨のフォーマットで文字列に変換する
     *
     * フォーマットパターンで利用できるプレースホルダ:
     * - `%amount%` : 桁区切り済みの金額（例: `1,234.56`）
     * - `%plural%` : 通貨の複数形
     * - `%symbol%` : 通貨の記号
     * - `%name%` : 通貨の名前
     *
     * @param amount フォーマットする金額
     * @return フォーマットされた金額文字列
     */
    fun format(amount: BigDecimal): String {
        // 桁区切り・小数点記号を適用した数値文字列を生成する
        val formattedAmount = CurrencyFormatter.format(
            amount = amount,
            fractionalDigits = fractionalDigits,
            thousandsSeparator = thousandsSeparator,
            decimalSeparator = decimalSeparator,
        )
        return format
            .replace("%amount%", formattedAmount)
            .replace("%plural%", plural)
            .replace("%symbol%", symbol)
            .replace("%name%", name)
    }

    /**
     * 金額をこの通貨の小数点桁数に丸める
     *
     * @param amount 丸める金額
     * @return 丸められた金額
     */
    fun round(amount: BigDecimal): BigDecimal {
        return amount.setScale(fractionalDigits, RoundingMode.HALF_UP)
    }
}
