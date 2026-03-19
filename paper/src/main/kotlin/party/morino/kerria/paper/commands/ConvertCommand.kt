package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.economy.ExchangeRateManager
import java.math.BigDecimal

/**
 * 通貨変換コマンド
 *
 * /kerria convert <amount> <fromCurrency> <toCurrency>
 */
@Command("kerria")
class ConvertCommand : KoinComponent {

    private val api: KerriaAPI by inject()
    private val exchangeRateManager: ExchangeRateManager by inject()

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
            sender.sendRichMessage("<red>このコマンドはプレイヤーのみが使用できます。")
            return
        }

        // 金額バリデーション
        if (amount <= 0) {
            sender.sendRichMessage("<red>金額は正の数を指定してください。")
            return
        }

        // 変換元通貨を名前から取得
        val from = api.getCurrencyManager().getCurrencyByName(fromCurrency).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨 <yellow>$fromCurrency</yellow> が見つかりません。")
            return
        }

        // 変換先通貨を名前から取得
        val to = api.getCurrencyManager().getCurrencyByName(toCurrency).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨 <yellow>$toCurrency</yellow> が見つかりません。")
            return
        }

        // アカウントを取得
        val account = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>アカウントが見つかりません。")
            return
        }

        // 通貨変換を実行
        val bigAmount = BigDecimal.valueOf(amount)
        exchangeRateManager.convert(account.accountId, from.id, to.id, bigAmount).fold(
            ifLeft = { error ->
                sender.sendRichMessage("<red>変換に失敗しました: ${error.message}")
            },
            ifRight = { convertedAmount ->
                val fromFormatted = from.format(bigAmount)
                val toFormatted = to.format(convertedAmount)
                sender.sendRichMessage("<green>${fromFormatted} を ${toFormatted} に変換しました。")
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
            sender.sendRichMessage("<red>通貨 <yellow>$fromCurrency</yellow> が見つかりません。")
            return
        }
        val to = api.getCurrencyManager().getCurrencyByName(toCurrency).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨 <yellow>$toCurrency</yellow> が見つかりません。")
            return
        }

        // レートを設定
        val bigRate = BigDecimal.valueOf(rate)
        exchangeRateManager.setRate(from.id, to.id, bigRate).fold(
            ifLeft = { error ->
                sender.sendRichMessage("<red>レートの設定に失敗しました: ${error.message}")
            },
            ifRight = {
                sender.sendRichMessage(
                    "<green>${from.name} → ${to.name} のレートを $rate に設定しました。",
                )
            },
        )
    }
}
