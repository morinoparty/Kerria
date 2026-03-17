package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI

/**
 * 残高確認コマンド
 *
 * /kerria balance [currencyId]
 */
@Command("kerria")
class BalanceCommand : KoinComponent {

    private val api: KerriaAPI by inject()

    @Command("balance [currencyId]")
    @Permission("kerria.balance")
    @Suppress("UnstableApiUsage")
    fun balance(stack: CommandSourceStack, @Default("1") currencyId: Int) {
        val sender = stack.sender
        if (sender !is Player) {
            sender.sendRichMessage("<red>このコマンドはプレイヤーのみが使用できます。")
            return
        }

        // プレイヤーのアカウントを取得
        val account = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>アカウントが見つかりません。")
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨が見つかりません。")
            return
        }

        // 残高を取得
        val balance = api.getAccountManager().getBalance(account.accountId, currency.id).getOrNull() ?: run {
            sender.sendRichMessage("<red>残高の取得に失敗しました。")
            return
        }

        // フォーマットして表示
        val formatted = currency.format(balance)
        sender.sendRichMessage("<green>あなたの残高は${formatted}です。")
    }
}
