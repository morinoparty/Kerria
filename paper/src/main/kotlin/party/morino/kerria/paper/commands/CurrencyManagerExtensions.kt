package party.morino.kerria.paper.commands

import arrow.core.Either
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.error.KerriaError

/**
 * コマンドの通貨ID引数から通貨を解決する
 *
 * 引数が省略された場合（null）は、設定ファイルの `economy.default-currency-id` が示すデフォルト通貨を使う。
 *
 * @param currencyId コマンドで指定された通貨ID（省略時は null）
 * @return 解決した通貨。見つからない場合は [KerriaError]
 */
fun CurrencyManager.getCurrencyOrDefault(currencyId: Int?): Either<KerriaError, Currency> =
    if (currencyId == null) getDefaultCurrency() else getCurrency(currencyId)
