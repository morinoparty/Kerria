package party.morino.kerria.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.Kerria
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.model.Currency


@Command("kerria")
class BalanceCommand : KoinComponent {

    val api : KerriaAPI by inject()

    @Command("balance [currency]")
    suspend fun balance(sender : CommandSender ,@Default("1") currency : Currency) {
        if(sender !is Player) {
            sender.sendRichMessage("<red>このコマンドはプレイヤーのみが使用できます。")
            return
        }
        val account = api.getAccountManager().getAccount(sender).getOrNull() ?: run{
            sender.sendRichMessage("<red>アカウントが見つかりません。")
            return
        }
        val currency = currency ?: return run{
            sender.sendRichMessage("<red>通貨が見つかりません。")
        }
        val balance = account.getBalance(currency)
        sender.sendRichMessage("<green>あなたの残高は${balance}円です。")
    }
}