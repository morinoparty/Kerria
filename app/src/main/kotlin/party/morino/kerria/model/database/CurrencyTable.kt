package party.morino.kerria.model.database

import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * 通貨テーブル
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
}