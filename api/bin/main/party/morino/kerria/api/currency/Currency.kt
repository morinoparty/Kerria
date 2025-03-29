package party.morino.kerria.api.currency

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal

/**
 * 通貨を表すインターフェース
 */
interface Currency {
    /**
     * 通貨のID
     */
    val currencyId: Int


    /**
     * 通貨の名前
     */
    val name: String

    /**
     * 通貨の複数形
     */
    val plural: String

    /**
     * 通貨の記号
     */
    val symbol: String

    /**
     * 小数点以下の桁数
     */
    val fractionalDigits: Int

    /**
     * 通貨のフォーマット
     */
    val format: String

    /**
     * 金額をフォーマットする
     * @param amount フォーマットする金額
     * @return フォーマットされた金額、もしくはエラー
     */
    suspend fun format(amount: BigDecimal): Either<KerriaError, String>

    /**
     * 金額を丸める
     * @param amount 丸める金額
     * @return 丸められた金額、もしくはエラー
     */
    suspend fun round(amount: BigDecimal): Either<KerriaError, BigDecimal>
}