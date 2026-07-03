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
import party.morino.kerria.api.files.MessageManager
import java.math.BigDecimal

/**
 * プレイヤー間送金コマンド
 *
 * /kerria pay <player> <amount> [currencyId]
 */
@Command("kerria")
class PayCommand : KoinComponent {

    private val api: KerriaAPI by inject()
    private val messages: MessageManager by inject()

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
            sender.sendRichMessage(messages.get("common.player-only"))
            return
        }

        // 金額バリデーション
        if (amount <= 0) {
            sender.sendRichMessage(messages.get("common.invalid-amount"))
            return
        }

        // 送金先アカウントを解決（キャッシュ非依存でオフラインプレイヤーも解決可能）
        val toAccount = TargetAccountResolver.resolve(api, player) ?: run {
            sender.sendRichMessage(messages.get("common.player-not-found", "player" to player))
            return
        }

        // 自分自身への送金チェック
        if (toAccount.playerUniqueId == sender.uniqueId) {
            sender.sendRichMessage(messages.get("pay.self"))
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendRichMessage(messages.get("common.currency-not-found"))
            return
        }

        // 送金元アカウントを取得
        val fromAccount = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage(messages.get("pay.sender-account-not-found"))
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
                sender.sendRichMessage(messages.get("pay.failed", "error" to (error.message ?: "")))
            },
            ifRight = {
                val formatted = currency.format(bigAmount)
                sender.sendRichMessage(
                    messages.get("pay.success", "player" to (toAccount.name ?: player), "amount" to formatted),
                )
                // 送金先がオンラインならメッセージを送信
                toAccount.playerUniqueId?.let { Bukkit.getPlayer(it) }?.sendRichMessage(
                    messages.get("pay.received", "player" to (sender.name), "amount" to formatted),
                )
            },
        )
    }
}
