package party.morino.kerria.paper.model

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.datetime
import party.morino.kerria.paper.model.database.AccountTable
import java.time.LocalDateTime

/**
 * 取引ログテーブルの定義
 *
 * このオブジェクトは取引履歴を格納するテーブルを定義します。
 * [LongIdTable]を継承し、自動インクリメントのLong型主キーを持ちます。
 * 送金元と送金先のアカウント、取引金額、取引時刻を記録します。
 *
 * @property fromAccountId 送金元アカウントのID（外部キー）
 * @property toAccountId 送金先アカウントのID（外部キー）
 * @property amount 取引金額
 * @property timestamp 取引時刻
 */
object TransactionLogTable : LongIdTable("transaction_logs") {
    val fromAccountId = reference("from_account_id", AccountTable)
    val toAccountId = reference("to_account_id", AccountTable)
    val amount: Column<Long> = long("amount")
    val timestamp: Column<LocalDateTime> = datetime("timestamp")
}
