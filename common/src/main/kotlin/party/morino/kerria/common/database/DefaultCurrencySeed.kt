package party.morino.kerria.common.database

/**
 * 通貨テーブルが空のときに作成する初期通貨の値
 *
 * 通貨の情報はデータベースだけで管理するため、設定ファイルではなくここに初期値を持つ。
 * 作成後の変更は `/kerria currency edit` で行う。
 */
object DefaultCurrencySeed {
    // 通貨名
    const val NAME = "JPY"
    // 通貨記号
    const val SYMBOL = "¥"
    // 通貨の複数形
    const val PLURAL = "円"
    // 通貨フォーマット
    const val FORMAT = "%amount% %plural%"
    // 小数点以下の桁数
    const val FRACTIONAL_DIGITS = 2
    // 3桁ごとの桁区切り文字
    const val THOUSANDS_SEPARATOR = ","
    // 小数点記号
    const val DECIMAL_SEPARATOR = "."
}
