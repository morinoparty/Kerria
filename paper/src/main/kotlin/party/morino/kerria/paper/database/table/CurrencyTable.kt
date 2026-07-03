package party.morino.kerria.paper.database.table

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

/**
 * 通貨テーブルの定義
 */
object CurrencyTable : IntIdTable("currencies") {
    // 通貨名
    val name = varchar("name", 50)
    // 通貨の複数形
    val plural = varchar("plural", 50)
    // 通貨記号
    val symbol = varchar("sign", 10)
    // 通貨フォーマット
    val format = varchar("format", 100)
    // 小数点以下の桁数
    val fractionalDigits = integer("fractional_digits")
    // 3桁ごとの桁区切り文字（既存行にはデフォルト値を適用）
    val thousandsSeparator = varchar("thousands_separator", 8).default(",")
    // 小数点記号（既存行にはデフォルト値を適用）
    val decimalSeparator = varchar("decimal_separator", 8).default(".")
}
