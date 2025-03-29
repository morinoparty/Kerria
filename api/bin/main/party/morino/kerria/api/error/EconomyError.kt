package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * 経済関連のエラー
 */
sealed class EconomyError : KerriaError() {
    /**
     * 残高が不足している場合のエラー
     * @property required 必要な金額
     * @property actual 実際の残高
     */
    data class InsufficientBalance(val required: Long, val actual: Long) : EconomyError()

    /**
     * 無効な取引額の場合のエラー
     * @property amount 無効な金額
     * @property reason 無効である理由
     */
    data class InvalidAmount(val amount: Long, val reason: String) : EconomyError()

    /**
     * 取引制限に達した場合のエラー
     * @property limit 制限値
     * @property current 現在の値
     */
    data class TransactionLimitReached(val limit: Long, val current: Long) : EconomyError()
} 