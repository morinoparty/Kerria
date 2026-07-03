package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.files.MessageManager

/**
 * 残高確認コマンド
 *
 * /kerria balance [currencyId]
 */
@Command("kerria")
class BalanceCommand : KoinComponent {

    private val api: KerriaAPI by inject()
    private val messages: MessageManager by inject()

    @Command("balance [currencyId]")
    @Permission("kerria.balance")
    @Suppress("UnstableApiUsage")
    fun balance(stack: CommandSourceStack, @Default("1") currencyId: Int) {
        val sender = stack.sender
        if (sender !is Player) {
            sender.sendMessage(messages.get("common.player-only"))
            return
        }

        // プレイヤーのアカウントを取得
        val account = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.account-not-found"))
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.currency-not-found"))
            return
        }

        // 残高を取得
        val balance = api.getAccountManager().getBalance(account.accountId, currency.id).getOrNull() ?: run {
            sender.sendMessage(messages.get("balance.fetch-failed"))
            return
        }

        // フォーマットして表示
        val formatted = currency.format(balance)
        sender.sendMessage(messages.get("balance.result", "amount" to formatted))
    }
}
