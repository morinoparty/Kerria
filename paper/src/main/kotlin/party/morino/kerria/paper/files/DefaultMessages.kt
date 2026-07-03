package party.morino.kerria.paper.files

/**
 * メッセージのデフォルトカタログ
 *
 * `messages.yml` が存在しない場合の初期値であり、キーの正本でもある。
 * キーはドット区切りのフラットな `Map<String, String>` として保持する
 * （kaml がネストではなくフラットなキーとして読み書きするため）。
 * 各値は MiniMessage 形式で、`<key>` 形式のプレースホルダを含みうる。
 */
object DefaultMessages {

    /** キー → MiniMessage テンプレートのデフォルト対応表 */
    val DEFAULT: Map<String, String> = linkedMapOf(
        // --- 共通 ---
        "common.player-only" to "<red>このコマンドはプレイヤーのみが使用できます。",
        "common.account-not-found" to "<red>アカウントが見つかりません。",
        "common.target-account-not-found" to "<red>対象のアカウントが見つかりません。",
        "common.currency-not-found" to "<red>通貨が見つかりません。",
        "common.player-not-found" to "<red>プレイヤー <yellow><player></yellow> が見つかりません。",
        "common.invalid-amount" to "<red>金額は正の数を指定してください。",

        // --- 残高確認 (balance) ---
        "balance.result" to "<green>あなたの残高は<amount>です。",
        "balance.fetch-failed" to "<red>残高の取得に失敗しました。",

        // --- 送金 (pay) ---
        "pay.self" to "<red>自分自身に送金することはできません。",
        "pay.sender-account-not-found" to "<red>あなたのアカウントが見つかりません。",
        "pay.target-account-not-found" to "<red>相手のアカウントが見つかりません。",
        "pay.failed" to "<red>送金に失敗しました: <error>",
        "pay.success" to "<green><yellow><player></yellow> に <amount> を送金しました。",
        "pay.received" to "<green><yellow><player></yellow> から <amount> を受け取りました。",

        // --- 管理者残高操作 (set/give/take) ---
        "admin.set.failed" to "<red>残高の設定に失敗しました: <error>",
        "admin.set.success" to "<green><yellow><player></yellow> の残高を <amount> に設定しました。",
        "admin.give.failed" to "<red>入金に失敗しました: <error>",
        "admin.give.success" to "<green><yellow><player></yellow> に <given> を付与しました。残高: <balance>",
        "admin.take.failed" to "<red>出金に失敗しました: <error>",
        "admin.take.success" to "<green><yellow><player></yellow> から <taken> を徴収しました。残高: <balance>",

        // --- 残高ランキング (top) ---
        "top.fetch-failed" to "<red>ランキングの取得に失敗しました。",
        "top.empty" to "<yellow>表示するデータがありません。",
        "top.header" to "<gold>===== 残高ランキング (<currency>) - ページ <page> =====",
        "top.entry" to "<yellow>#<rank> <white><name> <green><amount>",
        "top.next-page" to "<gray>次のページ: <click:run_command:'/kerria top <currencyId> <nextPage>'><aqua>[ページ <nextPage>]</click>",

        // --- 取引履歴 (log) ---
        "log.fetch-failed" to "<red>取引履歴の取得に失敗しました。",
        "log.empty" to "<yellow>表示する取引履歴がありません。",
        "log.header" to "<gold>===== <player>の取引履歴 - ページ <page> =====",
        "log.entry.incoming" to "<gray><time> <green>+<amount> <gray><plugin> <message>",
        "log.entry.outgoing" to "<gray><time> <red>-<amount> <gray><plugin> <message>",
        "log.next-page" to "<gray>次のページ: <click:run_command:'/kerria log <nextPage>'><aqua>[ページ <nextPage>]</click>",

        // --- 通貨管理 (currency) ---
        "currency.not-found" to "<red>通貨 <yellow><name></yellow> が見つかりません。",
        "currency.create.failed" to "<red>通貨の作成に失敗しました: <error>",
        "currency.create.success" to "<green>通貨 <yellow><name></yellow> (<symbol>) を作成しました。ID: <id>",
        "currency.edit.invalid-decimals" to "<red>小数桁数には 0 以上の整数を指定してください。",
        "currency.edit.unknown-property" to "<red>不明なプロパティです: <yellow><property></yellow>",
        "currency.edit.available-properties" to "<gray>指定可能: name, plural, symbol, format, fractionalDigits, thousandsSeparator, decimalSeparator",
        "currency.edit.failed" to "<red>通貨の更新に失敗しました: <error>",
        "currency.edit.success" to "<green>通貨 <yellow><name></yellow> の <white><property></white> を更新しました。",
        "currency.edit.example" to "<gray>表示例: <green><example>",
        "currency.delete.failed" to "<red>通貨の削除に失敗しました: <error>",
        "currency.delete.success" to "<green>通貨 <yellow><name></yellow> を削除しました。",
        "currency.list.failed" to "<red>通貨一覧の取得に失敗しました。",
        "currency.list.empty" to "<yellow>登録されている通貨がありません。",
        "currency.list.header" to "<gold>===== 通貨一覧 =====",
        "currency.list.entry" to "<yellow>#<id> <white><name> <gray>(<symbol>) 小数桁数: <digits>",
        "currency.info.header" to "<gold>===== 通貨情報 =====",
        "currency.info.id" to "<gray>ID: <white><id>",
        "currency.info.name" to "<gray>名前: <white><name>",
        "currency.info.plural" to "<gray>複数形: <white><plural>",
        "currency.info.symbol" to "<gray>記号: <white><symbol>",
        "currency.info.format" to "<gray>フォーマット: <white><format>",
        "currency.info.digits" to "<gray>小数桁数: <white><digits>",
        "currency.info.example" to "<gray>表示例: <green><example>",
        "currency.default.success" to "<green>デフォルト通貨を <yellow><name></yellow> に変更しました。",
        "currency.default.failed" to "<red>デフォルト通貨の変更に失敗しました: <error>",

        // --- 通貨変換・為替レート (convert / rate) ---
        "convert.failed" to "<red>変換に失敗しました: <error>",
        "convert.success" to "<green><from> を <to> に変換しました。",
        "rate.set.failed" to "<red>レートの設定に失敗しました: <error>",
        "rate.set.success" to "<green><from> → <to> のレートを <rate> に設定しました。",
        "rate.list.failed" to "<red>為替レートの取得に失敗しました。",
        "rate.list.empty" to "<gray>設定されている為替レートはありません。",
        "rate.list.header" to "<gold>為替レート一覧 (全 <count> 件)",
        "rate.list.entry" to "<white><from> <gray>→</gray> <white><to></white> <yellow><rate>",
    )
}
