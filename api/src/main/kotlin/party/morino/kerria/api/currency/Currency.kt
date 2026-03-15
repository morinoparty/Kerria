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
 */
data class Currency(
    val id: Int,
    val name: String,
    val plural: String,
    val symbol: String,
    val format: String,
    val fractionalDigits: Int,
) {
    /**
     * 金額をこの通貨のフォーマットで文字列に変換する
     *
     * @param amount フォーマットする金額
     * @return フォーマットされた金額文字列
     */
    fun format(amount: BigDecimal): String {
        val rounded = round(amount)
        return format
            .replace("%amount%", rounded.toPlainString())
            .replace("%plural%", plural)
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
