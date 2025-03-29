package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * データベース関連のエラー
 */
sealed class DatabaseError(message: String) : KerriaError(message) {
    /**
     * データベース接続エラー
     * @property message エラーメッセージ
     * @property cause 原因となった例外
     */
    class ConnectionError(override val message: String, override val cause: Throwable) : DatabaseError(message)

    /**
     * クエリ実行エラー
     * @property message エラーメッセージ
     * @property cause 原因となった例外
     */
    class QueryError(override val message: String, override val cause: Throwable) : DatabaseError(message)

    /**
     * データ整合性エラー
     * @property message エラーメッセージ
     * @property cause 原因となった例外
     */
    class IntegrityError(override val message: String, override val cause: Throwable) : DatabaseError(message)
} 