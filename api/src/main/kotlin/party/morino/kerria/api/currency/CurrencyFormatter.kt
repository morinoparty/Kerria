package party.morino.kerria.api.currency

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 金額の数値部分を桁区切り付きで整形する純粋なユーティリティ
 *
 * ロケールに依存せず決定的に整形するため、[java.text.DecimalFormat] は用いず
 * [BigDecimal.toPlainString] を分解して自前で桁区切り・小数点記号を適用する。
 */
object CurrencyFormatter {

    /**
     * 金額を桁区切り付きの数値文字列へ整形する
     *
     * 例: `format(BigDecimal("1234567.89"), 2, ",", ".")` は `"1,234,567.89"` を返す。
     *
     * @param amount 整形する金額
     * @param fractionalDigits 小数点以下の桁数（丸めは HALF_UP）
     * @param thousandsSeparator 3 桁ごとの区切り文字（空文字なら桁区切りしない）
     * @param decimalSeparator 小数点記号
     * @return 桁区切り済みの数値文字列
     */
    fun format(
        amount: BigDecimal,
        fractionalDigits: Int,
        thousandsSeparator: String,
        decimalSeparator: String,
    ): String {
        // 指定桁数に丸めてから文字列化する
        val rounded = amount.setScale(fractionalDigits, RoundingMode.HALF_UP)
        // 負号は末尾で戻すため、絶対値を対象に整形する
        val negative = rounded.signum() < 0
        val plain = rounded.abs().toPlainString()

        // 整数部と小数部を分離する（小数部が無い場合もある）
        val dotIndex = plain.indexOf('.')
        val integerPart = if (dotIndex >= 0) plain.substring(0, dotIndex) else plain
        val fractionPart = if (dotIndex >= 0) plain.substring(dotIndex + 1) else ""

        // 整数部へ 3 桁ごとの区切りを適用する
        val groupedInteger = groupIntegerPart(integerPart, thousandsSeparator)

        // 小数部があれば小数点記号で連結する
        val body = if (fractionPart.isNotEmpty()) {
            groupedInteger + decimalSeparator + fractionPart
        } else {
            groupedInteger
        }

        // 元が負数なら負号を戻す
        return if (negative) "-$body" else body
    }

    /**
     * 整数部の文字列に 3 桁ごとの区切り文字を挿入する
     *
     * @param integerPart 区切りを適用する整数部（符号なし）
     * @param separator 区切り文字（空文字なら何もしない）
     * @return 桁区切り済みの整数部
     */
    private fun groupIntegerPart(integerPart: String, separator: String): String {
        // 区切り不要、もしくは区切る桁が無い場合はそのまま返す
        if (separator.isEmpty() || integerPart.length <= 3) return integerPart

        val builder = StringBuilder()
        // 末尾から 3 桁ごとに区切りを差し込む
        val firstGroupSize = integerPart.length % 3
        var index = 0
        // 先頭の端数グループ（1〜2 桁）を先に出力する
        if (firstGroupSize > 0) {
            builder.append(integerPart, 0, firstGroupSize)
            index = firstGroupSize
        }
        // 以降は 3 桁ごとに区切り文字を挟んで出力する
        while (index < integerPart.length) {
            if (builder.isNotEmpty()) builder.append(separator)
            builder.append(integerPart, index, index + 3)
            index += 3
        }
        return builder.toString()
    }
}
