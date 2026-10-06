package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.economy.ExchangeRateManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.api.files.MessageManager
import java.math.BigDecimal

/**
 * 通貨変換・為替レート管理コマンド
 *
 * /kerria convert <amount> <fromCurrency> <toCurrency>
 * /kerria rate set <fromCurrency> <toCurrency> <rate>
 * /kerria rate delete <fromCurrency> <toCurrency>
 * /kerria rate list
 */
@Command("kerria")
class ConvertCommand : KoinComponent {

    private val api: KerriaAPI by inject()
    private val exchangeRateManager: ExchangeRateManager by inject()
    private val messages: MessageManager by inject()

    @Command("convert <amount> <fromCurrency> <toCurrency>")
    @Permission("kerria.convert")
    @Suppress("UnstableApiUsage")
    fun convert(
        stack: CommandSourceStack,
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
    ) {
        val sender = stack.sender
        if (sender !is Player) {
            sender.sendMessage(messages.get("common.player-only"))
            return
        }

        // 金額バリデーション
        if (amount <= 0) {
            sender.sendMessage(messages.get("common.invalid-amount"))
            return
        }

        // 変換元通貨を名前から取得
        val from = api.getCurrencyManager().getCurrencyByName(fromCurrency).getOrNull() ?: run {
            sender.sendMessage(messages.get("currency.not-found", "name" to fromCurrency))
            return
        }

        // 変換先通貨を名前から取得
        val to = api.getCurrencyManager().getCurrencyByName(toCurrency).getOrNull() ?: run {
            sender.sendMessage(messages.get("currency.not-found", "name" to toCurrency))
            return
        }

        // アカウントを取得
        val account = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.account-not-found"))
            return
        }

        // 通貨変換を実行
        val bigAmount = BigDecimal.valueOf(amount)
        exchangeRateManager.convert(account.accountId, from.id, to.id, bigAmount).fold(
            ifLeft = { error ->
                sender.sendMessage(messages.get("convert.failed", "error" to (error.message ?: "")))
            },
            ifRight = { convertedAmount ->
                val fromFormatted = from.format(bigAmount)
                val toFormatted = to.format(convertedAmount)
                sender.sendMessage(
                    messages.get("convert.success", "from" to fromFormatted, "to" to toFormatted),
                )
            },
        )
    }

    @Command("rate set <fromCurrency> <toCurrency> <rate>")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun setRate(
        stack: CommandSourceStack,
        fromCurrency: String,
        toCurrency: String,
        rate: Double,
    ) {
        val sender = stack.sender

        // 通貨を名前から取得
        val from = api.getCurrencyManager().getCurrencyByName(fromCurrency).getOrNull() ?: run {
            sender.sendMessage(messages.get("currency.not-found", "name" to fromCurrency))
            return
        }
        val to = api.getCurrencyManager().getCurrencyByName(toCurrency).getOrNull() ?: run {
            sender.sendMessage(messages.get("currency.not-found", "name" to toCurrency))
            return
        }

        // レートを設定
        val bigRate = BigDecimal.valueOf(rate)
        exchangeRateManager.setRate(from.id, to.id, bigRate).fold(
            ifLeft = { error ->
                sender.sendMessage(messages.get("rate.set.failed", "error" to (error.message ?: "")))
            },
            ifRight = {
                sender.sendMessage(
                    messages.get(
                        "rate.set.success",
                        "from" to from.name,
                        "to" to to.name,
                        "rate" to rate.toString(),
                    ),
                )
            },
        )
    }

    @Command("rate delete <fromCurrency> <toCurrency>")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun deleteRate(
        stack: CommandSourceStack,
        fromCurrency: String,
        toCurrency: String,
    ) {
        val sender = stack.sender

        // 通貨を名前から取得
        val from = api.getCurrencyManager().getCurrencyByName(fromCurrency).getOrNull() ?: run {
            sender.sendMessage(messages.get("currency.not-found", "name" to fromCurrency))
            return
        }
        val to = api.getCurrencyManager().getCurrencyByName(toCurrency).getOrNull() ?: run {
            sender.sendMessage(messages.get("currency.not-found", "name" to toCurrency))
            return
        }

        // レートを削除（存在しない場合は専用のメッセージを表示する）
        exchangeRateManager.deleteRate(from.id, to.id).fold(
            ifLeft = { error ->
                val message = if (error is KerriaError.CurrencyNotFound) {
                    messages.get("rate.delete.not-found", "from" to from.name, "to" to to.name)
                } else {
                    messages.get("rate.delete.failed", "error" to (error.message ?: ""))
                }
                sender.sendMessage(message)
            },
            ifRight = {
                sender.sendMessage(messages.get("rate.delete.success", "from" to from.name, "to" to to.name))
            },
        )
    }

    @Command("rate list")
    @Permission("kerria.admin.currency")
    @Suppress("UnstableApiUsage")
    fun listRates(stack: CommandSourceStack) {
        val sender = stack.sender

        // 設定済みの全レートを取得
        val rates = exchangeRateManager.getAllRates().getOrNull() ?: run {
            sender.sendMessage(messages.get("rate.list.failed"))
            return
        }
        if (rates.isEmpty()) {
            sender.sendMessage(messages.get("rate.list.empty"))
            return
        }

        // 通貨IDから通貨名を引くためのマップを構築（毎行のDBアクセスを避ける）
        val currencyNameById = api.getCurrencyManager().getAllCurrencies().getOrNull()
            .orEmpty()
            .associate { it.id to it.name }

        sender.sendMessage(messages.get("rate.list.header", "count" to rates.size.toString()))
        rates.forEach { rate ->
            // 通貨が削除済みの場合は ID をそのまま表示する
            val fromName = currencyNameById[rate.fromCurrencyId] ?: "#${rate.fromCurrencyId}"
            val toName = currencyNameById[rate.toCurrencyId] ?: "#${rate.toCurrencyId}"
            sender.sendMessage(
                messages.get(
                    "rate.list.entry",
                    "from" to fromName,
                    "to" to toName,
                    "rate" to rate.rate.toPlainString(),
                ),
            )
        }
    }
}
