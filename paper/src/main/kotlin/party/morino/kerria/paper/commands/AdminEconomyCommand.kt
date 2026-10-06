package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.files.MessageManager
import party.morino.kerria.paper.commands.target.PlayerTarget
import party.morino.kerria.paper.commands.target.TargetAccountResolver
import party.morino.kerria.paper.message.TransactionMessageFormatter
import java.math.BigDecimal

/**
 * 管理者用残高操作コマンド
 *
 * /kerria set <player|selector> <amount> [currencyId] [--message <text>]
 * /kerria give <player|selector> <amount> [currencyId] [--message <text>]
 * /kerria take <player|selector> <amount> [currencyId] [--message <text>]
 *
 * 対象にはプレイヤー名（オフライン可）または `@a` などのセレクタを指定できる。
 * `--message` は取引ログに記録される。give のみ MiniMessage 形式に対応し、受取人にも通知される。
 */
@Command("kerria")
class AdminEconomyCommand : KoinComponent {

    private val api: KerriaAPI by inject()
    private val messages: MessageManager by inject()

    @Command("set <player> <amount> [currencyId]")
    @Permission("kerria.admin.economy")
    @Suppress("UnstableApiUsage")
    fun set(
        stack: CommandSourceStack,
        player: PlayerTarget,
        amount: Double,
        @Default("1") currencyId: Int,
        @Flag("message") @Greedy message: String?,
    ) = execute(stack.sender, AdminBalanceOperation.SET, player, amount, currencyId, message)

    @Command("give <player> <amount> [currencyId]")
    @Permission("kerria.admin.economy")
    @Suppress("UnstableApiUsage")
    fun give(
        stack: CommandSourceStack,
        player: PlayerTarget,
        amount: Double,
        @Default("1") currencyId: Int,
        @Flag("message") @Greedy message: String?,
    ) = execute(stack.sender, AdminBalanceOperation.GIVE, player, amount, currencyId, message)

    @Command("take <player> <amount> [currencyId]")
    @Permission("kerria.admin.economy")
    @Suppress("UnstableApiUsage")
    fun take(
        stack: CommandSourceStack,
        player: PlayerTarget,
        amount: Double,
        @Default("1") currencyId: Int,
        @Flag("message") @Greedy message: String?,
    ) = execute(stack.sender, AdminBalanceOperation.TAKE, player, amount, currencyId, message)

    /**
     * 管理者用残高操作の共通処理
     *
     * 対象を解決し、各アカウントに操作を実行して結果を送信者へ通知する。
     */
    private fun execute(
        sender: CommandSender,
        operation: AdminBalanceOperation,
        target: PlayerTarget,
        amount: Double,
        currencyId: Int,
        message: String?,
    ) {
        // 取引メッセージは give のみ MiniMessage を許可し、set / take はプレーンテキストとして保存する
        val customMessage = message?.let {
            if (operation.allowsMiniMessage) {
                TransactionMessageFormatter.fromMiniMessage(it)
            } else {
                TransactionMessageFormatter.fromPlainText(it)
            }
        }
        if (customMessage != null && !TransactionMessageFormatter.isValid(customMessage)) {
            sender.sendMessage(
                messages.get("common.invalid-message", "max" to TransactionMessageFormatter.MAX_LENGTH.toString()),
            )
            return
        }

        // 対象アカウントを解決（名前はオフラインも可、セレクタはオンラインプレイヤーのみ）
        val accounts = try {
            TargetAccountResolver.resolveAll(api, sender, target)
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(messages.get("common.invalid-selector", "error" to (e.message ?: target.input)))
            return
        }
        if (accounts.isEmpty()) {
            val key = if (target.isSelector) "common.no-targets" else "common.player-not-found"
            sender.sendMessage(messages.get(key, "player" to target.input))
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.currency-not-found"))
            return
        }

        // 指定が無ければ従来どおり実行者を記録する
        val logMessage = customMessage ?: "${operation.logLabel} by ${sender.name}"
        val bigAmount = BigDecimal.valueOf(amount)

        // 複数対象の場合は個別の成功通知を省き、最後に件数をまとめて通知する
        val bulk = accounts.size > 1

        // 各アカウントに操作を実行し、成功件数を数える
        val succeeded = accounts.count { account ->
            applyTo(sender, operation, account, currency, bigAmount, logMessage, customMessage, bulk)
        }

        if (bulk) {
            sender.sendMessage(
                messages.get(
                    "admin.${operation.key}.success-multiple",
                    "count" to succeeded.toString(),
                    "total" to accounts.size.toString(),
                    "amount" to currency.format(bigAmount),
                ),
            )
        }
    }

    /**
     * 1つのアカウントに操作を実行し、結果を通知する
     *
     * 複数対象の一括処理中（bulk）は、個別の成功通知を送らない。
     * 成功した場合は true を返す。
     */
    @Suppress("LongParameterList")
    private fun applyTo(
        sender: CommandSender,
        operation: AdminBalanceOperation,
        account: Account,
        currency: Currency,
        amount: BigDecimal,
        logMessage: String,
        customMessage: String?,
        bulk: Boolean,
    ): Boolean {
        val playerName = account.name ?: account.accountId.toString()
        return operation.apply(api.getEconomyManager(), account.accountId, currency.id, amount, logMessage).fold(
            ifLeft = { error ->
                sender.sendMessage(
                    messages.get(
                        "admin.${operation.key}.failed",
                        "player" to playerName,
                        "error" to (error.message ?: ""),
                    ),
                )
                false
            },
            ifRight = { newBalance ->
                val formattedAmount = currency.format(amount)
                // 単一対象の場合のみ、従来どおり個別の成功通知を送る
                if (!bulk) {
                    sender.sendMessage(
                        messages.get(
                            "admin.${operation.key}.success",
                            "player" to playerName,
                            "amount" to formattedAmount,
                            "given" to formattedAmount,
                            "taken" to formattedAmount,
                            "balance" to currency.format(newBalance),
                        ),
                    )
                }
                if (operation == AdminBalanceOperation.GIVE) {
                    notifyReceiver(account, formattedAmount, customMessage)
                }
                true
            },
        )
    }

    /**
     * give の受取人がオンラインであれば、受け取った金額とメッセージを通知する
     */
    private fun notifyReceiver(account: Account, formattedAmount: String, customMessage: String?) {
        val receiver = account.playerUniqueId?.let { Bukkit.getPlayer(it) } ?: return
        // メッセージがあれば同じ行に添える（管理者が指定したため、MiniMessage の全タグを解釈する）
        val notice = if (customMessage == null) {
            messages.get("admin.give.received", "amount" to formattedAmount)
        } else {
            messages.get(
                "admin.give.received-with-message",
                mapOf("message" to TransactionMessageFormatter.render(customMessage)),
                "amount" to formattedAmount,
            )
        }
        receiver.sendMessage(notice)
    }
}
