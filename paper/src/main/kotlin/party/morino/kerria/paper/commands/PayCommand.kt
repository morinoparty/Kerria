package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import java.math.BigDecimal

/**
 * プレイヤー間送金コマンド
 *
 * /kerria pay <player> <amount> [currencyId]
 */
@Command("kerria")
class PayCommand : KoinComponent {

    private val api: KerriaAPI by inject()

    @Command("pay <player> <amount> [currencyId]")
    @Permission("kerria.pay")
    @Suppress("UnstableApiUsage")
    fun pay(
        stack: CommandSourceStack,
        player: String,
        amount: Double,
        @Default("1") currencyId: Int,
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

        // 送金先プレイヤーの検索
        val targetPlayer = Bukkit.getOfflinePlayerIfCached(player)
        if (targetPlayer == null) {
            sender.sendRichMessage("<red>プレイヤー <yellow>$player</yellow> が見つかりません。")
            return
        }

        // 自分自身への送金チェック
        if (targetPlayer.uniqueId == sender.uniqueId) {
            sender.sendRichMessage("<red>自分自身に送金することはできません。")
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨が見つかりません。")
            return
        }

        // 送金元アカウントを取得
        val fromAccount = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>あなたのアカウントが見つかりません。")
            return
        }

        // 送金先アカウントを取得
        val toAccount = api.getAccountManager().getAccount(targetPlayer.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>相手のアカウントが見つかりません。")
            return
        }

        // 送金を実行
        val bigAmount = BigDecimal.valueOf(amount)
        api.getEconomyManager().transfer(
            fromAccount.accountId,
            toAccount.accountId,
            currencyId,
            bigAmount,
            treatePluginName = "Kerria",
        ).fold(
            ifLeft = { error ->
                sender.sendRichMessage("<red>送金に失敗しました: ${error.message}")
            },
            ifRight = {
                val formatted = currency.format(bigAmount)
                sender.sendRichMessage("<green><yellow>${targetPlayer.name}</yellow> に ${formatted} を送金しました。")
                // 送金先がオンラインならメッセージを送信
                targetPlayer.player?.sendRichMessage(
                    "<green><yellow>${sender.name}</yellow> から ${formatted} を受け取りました。",
                )
            },
        )
    }
}
