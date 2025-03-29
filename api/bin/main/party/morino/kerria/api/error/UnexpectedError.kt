package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * その他の予期せぬエラー
 * @property message エラーメッセージ
 * @property cause 原因となった例外（存在する場合）
 */
data class UnexpectedError(override val message: String, override val cause: Throwable? = null) : KerriaError(message) 