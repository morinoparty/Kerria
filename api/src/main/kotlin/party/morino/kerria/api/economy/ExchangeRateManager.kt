package party.morino.kerria.api.economy

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal

/**
 * 通貨間の為替レートを管理するインターフェース
 *
 * レートの取得・設定・削除の機能を提供します。
 */
interface ExchangeRateManager {

    /**
     * 通貨ペアの為替レートを取得する
     *
     * @param fromCurrencyId 変換元通貨ID
     * @param toCurrencyId 変換先通貨ID
     * @return 為替レート、もしくはエラー
     */
    fun getRate(fromCurrencyId: Int, toCurrencyId: Int): Either<KerriaError, BigDecimal>

    /**
     * 為替レートを設定する（既存の場合は更新）
     *
     * @param fromCurrencyId 変換元通貨ID
     * @param toCurrencyId 変換先通貨ID
     * @param rate 為替レート
     * @return 成功時はUnit、失敗時はエラー
     */
    fun setRate(fromCurrencyId: Int, toCurrencyId: Int, rate: BigDecimal): Either<KerriaError, Unit>

    /**
     * 通貨変換を実行する
     *
     * 変換元通貨から出金し、レートに基づいて変換先通貨に入金する。
     *
     * @param accountId 対象アカウントID
     * @param fromCurrencyId 変換元通貨ID
     * @param toCurrencyId 変換先通貨ID
     * @param amount 変換する金額（変換元通貨の額）
     * @return 変換先通貨で受け取った金額、もしくはエラー
     */
    fun convert(
        accountId: java.util.UUID,
        fromCurrencyId: Int,
        toCurrencyId: Int,
        amount: BigDecimal,
    ): Either<KerriaError, BigDecimal>
}
