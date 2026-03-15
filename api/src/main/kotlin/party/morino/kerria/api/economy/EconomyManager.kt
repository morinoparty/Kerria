package party.morino.kerria.api.economy

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal
import java.util.UUID

/**
 * 経済操作の中心となるインターフェース
 *
 * 入金・出金・送金のビジネスロジックを提供します。
 * バリデーション、残高チェック、取引ログ記録を一元管理します。
 */
interface EconomyManager {

    /**
     * 指定アカウントに入金する
     *
     * @param accountId 入金先アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 入金額
     * @return 入金後の残高、もしくはエラー
     */
    fun deposit(accountId: UUID, currencyId: Int, amount: BigDecimal): Either<KerriaError, BigDecimal>

    /**
     * 指定アカウントから出金する
     *
     * @param accountId 出金元アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 出金額
     * @return 出金後の残高、もしくはエラー
     */
    fun withdraw(accountId: UUID, currencyId: Int, amount: BigDecimal): Either<KerriaError, BigDecimal>

    /**
     * アカウント間で送金する
     *
     * @param fromAccountId 送金元アカウントのUUID
     * @param toAccountId 送金先アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 送金額
     * @return 成功時はUnit、失敗時はエラー
     */
    fun transfer(
        fromAccountId: UUID,
        toAccountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
    ): Either<KerriaError, Unit>
}
