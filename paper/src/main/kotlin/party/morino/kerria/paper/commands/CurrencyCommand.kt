package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI

/**
 * 通貨管理コマンド
 *
 * /kerria currency create <name> <symbol> [decimals]
 * /kerria currency delete <name>
 * /kerria currency list
 * /kerria currency info <name>
 */
@Command("kerria currency")
class CurrencyCommand : KoinComponent {

    private val api: KerriaAPI by inject()

    @Command("create <name> <symbol> [decimals]")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun create(
        stack: CommandSourceStack,
        name: String,
        symbol: String,
        @Default("2") decimals: Int,
    ) {
        val sender = stack.sender

        // デフォルトフォーマット: "100 円" のような形式
        val format = "%amount% $name"
        val plural = name

        api.getCurrencyManager().createCurrency(name, symbol, format, decimals, plural).fold(
            ifLeft = { error ->
                sender.sendRichMessage("<red>通貨の作成に失敗しました: ${error.message}")
            },
            ifRight = { currency ->
                sender.sendRichMessage(
                    "<green>通貨 <yellow>${currency.name}</yellow> (${currency.symbol}) を作成しました。ID: ${currency.id}",
                )
            },
        )
    }

    @Command("delete <name>")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun delete(stack: CommandSourceStack, name: String) {
        val sender = stack.sender

        // 通貨名から通貨を取得
        val currency = api.getCurrencyManager().getCurrencyByName(name).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨 <yellow>$name</yellow> が見つかりません。")
            return
        }

        api.getCurrencyManager().deleteCurrency(currency.id).fold(
            ifLeft = { error ->
                sender.sendRichMessage("<red>通貨の削除に失敗しました: ${error.message}")
            },
            ifRight = {
                sender.sendRichMessage("<green>通貨 <yellow>${currency.name}</yellow> を削除しました。")
            },
        )
    }

    @Command("list")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun list(stack: CommandSourceStack) {
        val sender = stack.sender

        val currencies = api.getCurrencyManager().getAllCurrencies().getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨一覧の取得に失敗しました。")
            return
        }

        if (currencies.isEmpty()) {
            sender.sendRichMessage("<yellow>登録されている通貨がありません。")
            return
        }

        sender.sendRichMessage("<gold>===== 通貨一覧 =====")
        currencies.forEach { currency ->
            sender.sendRichMessage(
                "<yellow>#${currency.id} <white>${currency.name} <gray>(${currency.symbol}) 小数桁数: ${currency.fractionalDigits}",
            )
        }
    }

    @Command("info <name>")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun info(stack: CommandSourceStack, name: String) {
        val sender = stack.sender

        val currency = api.getCurrencyManager().getCurrencyByName(name).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨 <yellow>$name</yellow> が見つかりません。")
            return
        }

        sender.sendRichMessage("<gold>===== 通貨情報 =====")
        sender.sendRichMessage("<gray>ID: <white>${currency.id}")
        sender.sendRichMessage("<gray>名前: <white>${currency.name}")
        sender.sendRichMessage("<gray>複数形: <white>${currency.plural}")
        sender.sendRichMessage("<gray>記号: <white>${currency.symbol}")
        sender.sendRichMessage("<gray>フォーマット: <white>${currency.format}")
        sender.sendRichMessage("<gray>小数桁数: <white>${currency.fractionalDigits}")
        sender.sendRichMessage("<gray>表示例: <green>${currency.format(java.math.BigDecimal("1234.56"))}")
    }
}
