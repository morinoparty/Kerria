package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * データベース関連のエラー
 */
sealed class DatabaseError : KerriaError() {
    /**
     * データベース接続エラー
     * @property message エラーメッセージ
     * @property cause 原因となった例外
     */
    data class ConnectionError(val message: String, val cause: Throwable) : DatabaseError()

    /**
     * クエリ実行エラー
     * @property message エラーメッセージ
     * @property cause 原因となった例外
     */
    data class QueryError(val message: String, val cause: Throwable) : DatabaseError()

    /**
     * データ整合性エラー
     * @property message エラーメッセージ
     * @property cause 原因となった例外
     */
    data class IntegrityError(val message: String, val cause: Throwable) : DatabaseError()
} 