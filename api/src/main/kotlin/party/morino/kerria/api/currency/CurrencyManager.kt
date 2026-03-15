package party.morino.kerria.api.currency

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError

/**
 * 通貨を管理するインターフェース
 *
 * 通貨の取得・作成・一覧取得の機能を提供します。
 */
interface CurrencyManager {

    /**
     * 通貨IDから通貨を取得する
     *
     * @param id 通貨のID
     * @return 通貨、もしくはエラー
     */
    fun getCurrency(id: Int): Either<KerriaError, Currency>

    /**
     * デフォルトの通貨を取得する
     *
     * @return デフォルトの通貨、もしくはエラー
     */
    fun getDefaultCurrency(): Either<KerriaError, Currency>

    /**
     * 通貨を作成する
     *
     * @param name 通貨の名前
     * @param symbol 通貨のシンボル
     * @param format 通貨のフォーマット
     * @param decimals 小数点以下の桁数
     * @param plural 通貨の複数形
     * @return 作成された通貨、もしくはエラー
     */
    fun createCurrency(
        name: String,
        symbol: String,
        format: String,
        decimals: Int,
        plural: String,
    ): Either<KerriaError, Currency>

    /**
     * 全ての通貨を取得する
     *
     * @return 通貨のリスト、もしくはエラー
     */
    fun getAllCurrencies(): Either<KerriaError, List<Currency>>
}
