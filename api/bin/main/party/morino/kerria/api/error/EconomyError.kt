package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * 経済関連のエラー
 */
sealed class EconomyError(message: String) : KerriaError(message) {
    /**
     * 残高が不足している場合のエラー
     * @property required 必要な金額
     * @property actual 実際の残高
     */
    class InsufficientBalance(val required: Long, val actual: Long) : EconomyError("Insufficient balance. Required: $required, Actual: $actual")

    /**
     * 無効な取引額の場合のエラー
     * @property amount 無効な金額
     * @property reason 無効である理由
     */
    class InvalidAmount(val amount: Long, val reason: String) : EconomyError("Invalid amount: $amount, reason: $reason")

    /**
     * 取引制限に達した場合のエラー
     * @property limit 制限値
     * @property current 現在の値
     */
    class TransactionLimitReached(val limit: Long, val current: Long) : EconomyError("Transaction limit reached. Limit: $limit, Current: $current")
} 