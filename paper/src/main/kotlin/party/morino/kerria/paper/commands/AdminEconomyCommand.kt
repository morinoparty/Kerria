package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.files.MessageManager
import java.math.BigDecimal

/**
 * 管理者用残高操作コマンド
 *
 * /kerria set <player> <amount> [currencyId]
 * /kerria give <player> <amount> [currencyId]
 * /kerria take <player> <amount> [currencyId]
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
        player: String,
        amount: Double,
        @Default("1") currencyId: Int,
    ) {
        val sender = stack.sender

        // 対象アカウントを解決（キャッシュ非依存でオフラインプレイヤーも解決可能）
        val account = TargetAccountResolver.resolve(api, player) ?: run {
            sender.sendMessage(messages.get("common.player-not-found", "player" to player))
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.currency-not-found"))
            return
        }

        // 残高を設定
        val bigAmount = BigDecimal.valueOf(amount)
        api.getEconomyManager().setBalance(
            account.accountId,
            currencyId,
            bigAmount,
            message = "Admin set by ${sender.name}",
            treatePluginName = "Kerria",
        ).fold(
            ifLeft = { error ->
                sender.sendMessage(messages.get("admin.set.failed", "error" to (error.message ?: "")))
            },
            ifRight = { newBalance ->
                val formatted = currency.format(newBalance)
                sender.sendMessage(
                    messages.get("admin.set.success", "player" to (account.name ?: player), "amount" to formatted),
                )
            },
        )
    }

    @Command("give <player> <amount> [currencyId]")
    @Permission("kerria.admin.economy")
    @Suppress("UnstableApiUsage")
    fun give(
        stack: CommandSourceStack,
        player: String,
        amount: Double,
        @Default("1") currencyId: Int,
    ) {
        val sender = stack.sender

        // 対象アカウントを解決（キャッシュ非依存でオフラインプレイヤーも解決可能）
        val account = TargetAccountResolver.resolve(api, player) ?: run {
            sender.sendMessage(messages.get("common.player-not-found", "player" to player))
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.currency-not-found"))
            return
        }

        // 入金を実行
        val bigAmount = BigDecimal.valueOf(amount)
        api.getEconomyManager().deposit(
            account.accountId,
            currencyId,
            bigAmount,
            message = "Admin give by ${sender.name}",
            treatePluginName = "Kerria",
        ).fold(
            ifLeft = { error ->
                sender.sendMessage(messages.get("admin.give.failed", "error" to (error.message ?: "")))
            },
            ifRight = { newBalance ->
                sender.sendMessage(
                    messages.get(
                        "admin.give.success",
                        "player" to (account.name ?: player),
                        "given" to currency.format(bigAmount),
                        "balance" to currency.format(newBalance),
                    ),
                )
            },
        )
    }

    @Command("take <player> <amount> [currencyId]")
    @Permission("kerria.admin.economy")
    @Suppress("UnstableApiUsage")
    fun take(
        stack: CommandSourceStack,
        player: String,
        amount: Double,
        @Default("1") currencyId: Int,
    ) {
        val sender = stack.sender

        // 対象アカウントを解決（キャッシュ非依存でオフラインプレイヤーも解決可能）
        val account = TargetAccountResolver.resolve(api, player) ?: run {
            sender.sendMessage(messages.get("common.player-not-found", "player" to player))
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.currency-not-found"))
            return
        }

        // 出金を実行
        val bigAmount = BigDecimal.valueOf(amount)
        api.getEconomyManager().withdraw(
            account.accountId,
            currencyId,
            bigAmount,
            message = "Admin take by ${sender.name}",
            treatePluginName = "Kerria",
        ).fold(
            ifLeft = { error ->
                sender.sendMessage(messages.get("admin.take.failed", "error" to (error.message ?: "")))
            },
            ifRight = { newBalance ->
                sender.sendMessage(
                    messages.get(
                        "admin.take.success",
                        "player" to (account.name ?: player),
                        "taken" to currency.format(bigAmount),
                        "balance" to currency.format(newBalance),
                    ),
                )
            },
        )
    }
}
