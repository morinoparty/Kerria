package party.morino.kerria.api.error

/**
 * Kerriaプラグインのエラー型
 */
sealed class KerriaError(message: String) : Exception(message) {
    /**
     * 通貨が見つからない場合のエラー
     */
    class CurrencyNotFound(message: String) : KerriaError(message)

    /**
     * データベースエラー
     */
    class DatabaseError(message: String) : KerriaError(message)
}