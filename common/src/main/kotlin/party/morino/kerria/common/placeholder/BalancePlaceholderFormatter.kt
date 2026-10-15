package party.morino.kerria.common.placeholder

import party.morino.kerria.api.currency.Currency
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 残高プレースホルダーの表示文字列を組み立てる純粋関数群
 */
object BalancePlaceholderFormatter {
    /** 桁数として指定できる最大値（通貨の桁数より大きい値は意味が無いが、異常値は弾く） */
    const val MAX_DIGITS = 10

    /**
     * 残高を通貨フォーマット付きで表示する文字列にする
     *
     * @param currency 通貨
     * @param balance 残高
     * @param digits 表示する小数点以下の桁数。null なら通貨の桁数
     * @return 通貨フォーマットを適用した文字列（例: `1,234.56 円`）
     */
    fun formatted(currency: Currency, balance: BigDecimal, digits: Int?): String {
        if (digits == null) return currency.format(balance)
        // 表示する桁数で切り捨ててから、桁数だけを変えた通貨でフォーマットする
        return currency.copy(fractionalDigits = digits).format(truncate(balance, digits))
    }

    /**
     * 残高を数値のみの文字列にする
     *
     * @param currency 通貨
     * @param balance 残高
     * @param digits 表示する小数点以下の桁数。null なら通貨の桁数
     * @return 数値のみの文字列（例: `1234.56`）
     */
    fun raw(currency: Currency, balance: BigDecimal, digits: Int?): String =
        if (digits == null) currency.round(balance).toPlainString() else truncate(balance, digits).toPlainString()

    /**
     * プレースホルダーの引数から桁数を読み取る
     *
     * @param value 引数の文字列
     * @return 0 以上 [MAX_DIGITS] 以下の整数であればその値、それ以外は null
     */
    fun parseDigits(value: String?): Int? = value?.toIntOrNull()?.takeIf { it in 0..MAX_DIGITS }

    /**
     * 指定した桁数で切り捨てる
     *
     * 実際の所持金より多く見えないよう、四捨五入ではなく切り捨てにする。
     */
    private fun truncate(balance: BigDecimal, digits: Int): BigDecimal = balance.setScale(digits, RoundingMode.DOWN)
}
