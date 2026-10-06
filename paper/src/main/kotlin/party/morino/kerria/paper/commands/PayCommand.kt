package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.files.MessageManager
import party.morino.kerria.paper.commands.target.TargetAccountResolver
import party.morino.kerria.paper.message.TransactionMessageFormatter
import java.math.BigDecimal

/**
 * プレイヤー間送金コマンド
 *
 * /pay <player> <amount> [currencyId] [--message <text>]
 * /kerria pay <player> <amount> [currencyId] [--message <text>]
 *
 * `--message` はプレーンテキストとして扱い、MiniMessage のタグは解釈しない。
 */
class PayCommand : KoinComponent {

    private val api: KerriaAPI by inject()
    private val messages: MessageManager by inject()

    @Command("pay <player> <amount> [currencyId]")
    @Command("kerria pay <player> <amount> [currencyId]")
    @Permission("kerria.pay")
    @Suppress("UnstableApiUsage")
    fun pay(
        stack: CommandSourceStack,
        player: String,
        amount: Double,
        @Default("1") currencyId: Int,
        @Flag("message") @Greedy message: String?,
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

        // 取引メッセージはタグをエスケープしたうえで長さを検証する（プレイヤーにはタグを許可しない）
        val storedMessage = message?.let { TransactionMessageFormatter.fromPlainText(it) }
        if (storedMessage != null && !TransactionMessageFormatter.isValid(storedMessage)) {
            sender.sendMessage(
                messages.get("common.invalid-message", "max" to TransactionMessageFormatter.MAX_LENGTH.toString()),
            )
            return
        }

        // 送金先アカウントを解決（キャッシュ非依存でオフラインプレイヤーも解決可能）
        val toAccount = TargetAccountResolver.resolve(api, player) ?: run {
            sender.sendMessage(messages.get("common.player-not-found", "player" to player))
            return
        }

        // 自分自身への送金チェック
        if (toAccount.playerUniqueId == sender.uniqueId) {
            sender.sendMessage(messages.get("pay.self"))
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.currency-not-found"))
            return
        }

        // 送金元アカウントを取得
        val fromAccount = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendMessage(messages.get("pay.sender-account-not-found"))
            return
        }

        // 送金を実行
        val bigAmount = BigDecimal.valueOf(amount)
        api.getEconomyManager().transfer(
            fromAccount.accountId,
            toAccount.accountId,
            currencyId,
            bigAmount,
            message = storedMessage,
            treatePluginName = "Kerria",
        ).fold(
            ifLeft = { error ->
                sender.sendMessage(messages.get("pay.failed", "error" to (error.message ?: "")))
            },
            ifRight = {
                val formatted = currency.format(bigAmount)
                // 送金者への通知（メッセージがあれば同じ行に添える）
                sender.sendMessage(
                    withTransactionMessage(
                        "pay.success",
                        storedMessage,
                        "player" to (toAccount.name ?: player),
                        "amount" to formatted,
                    ),
                )
                // 送金先がオンラインなら受取通知を送信
                toAccount.playerUniqueId?.let { Bukkit.getPlayer(it) }?.sendMessage(
                    withTransactionMessage("pay.received", storedMessage, "player" to sender.name, "amount" to formatted),
                )
            },
        )
    }

    /**
     * 取引メッセージの有無に応じて、通知メッセージを1行で組み立てる
     *
     * メッセージがある場合は `<key>-with-message` を使い、末尾に「(メッセージ: ...)」を添える。
     * 保存形式はエスケープ済みのため、装飾系タグのみの解釈でもプレーンテキストとして表示される。
     */
    private fun withTransactionMessage(
        key: String,
        storedMessage: String?,
        vararg placeholders: Pair<String, String>,
    ): Component {
        storedMessage ?: return messages.get(key, *placeholders)
        return messages.get(
            "$key-with-message",
            mapOf("message" to TransactionMessageFormatter.renderStyleOnly(storedMessage)),
            *placeholders,
        )
    }
}
