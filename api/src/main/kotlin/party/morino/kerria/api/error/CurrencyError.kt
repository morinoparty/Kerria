package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * 通貨関連のエラー
 */
sealed class CurrencyError(message: String) : KerriaError(message) {
    /**
     * 通貨が見つからない場合のエラー
     * @property currencyId 見つからなかった通貨のID
     */
    class CurrencyNotFound(val currencyId: String) : CurrencyError("Currency not found: $currencyId")
}
