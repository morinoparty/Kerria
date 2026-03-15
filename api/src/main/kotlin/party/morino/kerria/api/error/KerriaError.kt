package party.morino.kerria.api.error

import java.math.BigDecimal

/**
 * Kerriaプラグインの全エラー型を定義する sealed hierarchy
 *
 * 各サブクラスはエラーの種類ごとに分類され、
 * Either<KerriaError, T> パターンで型安全なエラーハンドリングを実現します。
 */
sealed class KerriaError(message: String) : Exception(message) {

    // --- Player ---

    /** 指定されたUUIDのプレイヤーが見つからない */
    class PlayerNotFound(val uuid: String) :
        KerriaError("Player not found: $uuid")

    /** 指定されたUUIDのプレイヤーが既に存在する */
    class PlayerAlreadyExists(val uuid: String) :
        KerriaError("Player already exists: $uuid")

    // --- Economy ---

    /** 残高不足 */
    class InsufficientBalance(val required: BigDecimal, val actual: BigDecimal) :
        KerriaError("Insufficient balance: required=$required, actual=$actual")

    /** 不正な金額 */
    class InvalidAmount(val amount: BigDecimal, val reason: String) :
        KerriaError("Invalid amount: $amount ($reason)")

    // --- Currency ---

    /** 通貨が見つからない */
    class CurrencyNotFound(val currencyId: String) :
        KerriaError("Currency not found: $currencyId")

    // --- Database ---

    /** データベース操作エラー */
    class DatabaseError(override val message: String, override val cause: Throwable? = null) :
        KerriaError(message)

    // --- Config ---

    /** 設定ファイル読み込みエラー */
    class ConfigLoadError(override val message: String) :
        KerriaError(message)

    // --- General ---

    /** 予期しないエラー */
    class UnexpectedError(override val message: String, override val cause: Throwable? = null) :
        KerriaError(message)
}
