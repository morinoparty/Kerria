package party.morino.kerria.paper.database.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.datetime

/**
 * 取引ログテーブルの定義
 *
 * 送金元・送金先のアカウント、通貨、金額、時刻を記録する。
 */
object TransactionLogTable : LongIdTable("transaction_logs") {
    // 送金元アカウントへの外部キー
    val fromAccountId = reference("from_account_id", AccountTable)
    // 送金先アカウントへの外部キー
    val toAccountId = reference("to_account_id", AccountTable)
    // 通貨への外部キー
    val currencyId = reference("currency_id", CurrencyTable)
    // 取引金額
    val amount = decimal("amount", precision = 20, scale = 4)
    // 取引メモ・説明
    val message = varchar("message", 512).nullable()
    // 取引時刻
    val timestamp = datetime("timestamp")
}
