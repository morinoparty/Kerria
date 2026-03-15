package party.morino.kerria.paper.database.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

/**
 * アカウント残高テーブルの定義
 *
 * 通貨ごとの残高を管理する中間テーブル。
 * (accountId, currencyId) の組み合わせで一意。
 */
object AccountBalanceTable : LongIdTable("account_balances") {
    // 対象アカウントへの外部キー
    val accountId = reference("account_id", AccountTable)
    // 対象通貨への外部キー
    val currencyId = reference("currency_id", CurrencyTable)
    // 残高
    val balance = decimal("balance", precision = 20, scale = 4)

    init {
        uniqueIndex(accountId, currencyId)
    }
}
