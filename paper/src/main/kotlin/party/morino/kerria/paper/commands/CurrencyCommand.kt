package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.files.MessageManager

/**
 * 通貨管理コマンド
 *
 * /kerria currency create <name> <symbol> [decimals]
 * /kerria currency edit <name> <property> <newValue>
 * /kerria currency delete <name>
 * /kerria currency list
 * /kerria currency info <name>
 */
@Command("kerria currency")
class CurrencyCommand : KoinComponent {

    private val api: KerriaAPI by inject()
    private val messages: MessageManager by inject()

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
                sender.sendRichMessage(messages.get("currency.create.failed", "error" to (error.message ?: "")))
            },
            ifRight = { currency ->
                sender.sendRichMessage(
                    messages.get(
                        "currency.create.success",
                        "name" to currency.name,
                        "symbol" to currency.symbol,
                        "id" to currency.id.toString(),
                    ),
                )
            },
        )
    }

    @Command("edit <name> <property> <newValue>")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun edit(stack: CommandSourceStack, name: String, property: String, newValue: String) {
        val sender = stack.sender

        // 対象の通貨を名前から取得する
        val currency = api.getCurrencyManager().getCurrencyByName(name).getOrNull() ?: run {
            sender.sendRichMessage(messages.get("currency.not-found", "name" to name))
            return
        }

        // 指定されたプロパティに応じて更新後の通貨を組み立てる（不正な場合は null）
        val updated = when (property.lowercase()) {
            "name" -> currency.copy(name = newValue)
            "plural" -> currency.copy(plural = newValue)
            "symbol" -> currency.copy(symbol = newValue)
            "format" -> currency.copy(format = newValue)
            "fractionaldigits", "decimals" -> {
                // 小数桁数は 0 以上の整数のみ許可する
                val digits = newValue.toIntOrNull()
                if (digits == null || digits < 0) {
                    sender.sendRichMessage(messages.get("currency.edit.invalid-decimals"))
                    return
                }
                currency.copy(fractionalDigits = digits)
            }
            "thousandsseparator" -> currency.copy(thousandsSeparator = newValue)
            "decimalseparator" -> currency.copy(decimalSeparator = newValue)
            else -> {
                sender.sendRichMessage(messages.get("currency.edit.unknown-property", "property" to property))
                sender.sendRichMessage(messages.get("currency.edit.available-properties"))
                return
            }
        }

        api.getCurrencyManager().updateCurrency(updated).fold(
            ifLeft = { error ->
                sender.sendRichMessage(messages.get("currency.edit.failed", "error" to (error.message ?: "")))
            },
            ifRight = { result ->
                sender.sendRichMessage(
                    messages.get("currency.edit.success", "name" to result.name, "property" to property),
                )
                sender.sendRichMessage(
                    messages.get("currency.edit.example", "example" to result.format(java.math.BigDecimal("1234.56"))),
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
            sender.sendRichMessage(messages.get("currency.not-found", "name" to name))
            return
        }

        api.getCurrencyManager().deleteCurrency(currency.id).fold(
            ifLeft = { error ->
                sender.sendRichMessage(messages.get("currency.delete.failed", "error" to (error.message ?: "")))
            },
            ifRight = {
                sender.sendRichMessage(messages.get("currency.delete.success", "name" to currency.name))
            },
        )
    }

    @Command("list")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun list(stack: CommandSourceStack) {
        val sender = stack.sender

        val currencies = api.getCurrencyManager().getAllCurrencies().getOrNull() ?: run {
            sender.sendRichMessage(messages.get("currency.list.failed"))
            return
        }

        if (currencies.isEmpty()) {
            sender.sendRichMessage(messages.get("currency.list.empty"))
            return
        }

        sender.sendRichMessage(messages.get("currency.list.header"))
        currencies.forEach { currency ->
            sender.sendRichMessage(
                messages.get(
                    "currency.list.entry",
                    "id" to currency.id.toString(),
                    "name" to currency.name,
                    "symbol" to currency.symbol,
                    "digits" to currency.fractionalDigits.toString(),
                ),
            )
        }
    }

    @Command("info <name>")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun info(stack: CommandSourceStack, name: String) {
        val sender = stack.sender

        val currency = api.getCurrencyManager().getCurrencyByName(name).getOrNull() ?: run {
            sender.sendRichMessage(messages.get("currency.not-found", "name" to name))
            return
        }

        sender.sendRichMessage(messages.get("currency.info.header"))
        sender.sendRichMessage(messages.get("currency.info.id", "id" to currency.id.toString()))
        sender.sendRichMessage(messages.get("currency.info.name", "name" to currency.name))
        sender.sendRichMessage(messages.get("currency.info.plural", "plural" to currency.plural))
        sender.sendRichMessage(messages.get("currency.info.symbol", "symbol" to currency.symbol))
        sender.sendRichMessage(messages.get("currency.info.format", "format" to currency.format))
        sender.sendRichMessage(messages.get("currency.info.digits", "digits" to currency.fractionalDigits.toString()))
        sender.sendRichMessage(
            messages.get("currency.info.example", "example" to currency.format(java.math.BigDecimal("1234.56"))),
        )
    }
}
