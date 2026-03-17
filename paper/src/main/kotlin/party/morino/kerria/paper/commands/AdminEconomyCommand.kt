package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
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

        // 対象プレイヤーを検索
        val targetPlayer = Bukkit.getOfflinePlayerIfCached(player)
        if (targetPlayer == null) {
            sender.sendRichMessage("<red>プレイヤー <yellow>$player</yellow> が見つかりません。")
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨が見つかりません。")
            return
        }

        // 対象アカウントを取得
        val account = api.getAccountManager().getAccount(targetPlayer.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>対象のアカウントが見つかりません。")
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
                sender.sendRichMessage("<red>残高の設定に失敗しました: ${error.message}")
            },
            ifRight = { newBalance ->
                val formatted = currency.format(newBalance)
                sender.sendRichMessage(
                    "<green><yellow>${targetPlayer.name}</yellow> の残高を ${formatted} に設定しました。",
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

        // 対象プレイヤーを検索
        val targetPlayer = Bukkit.getOfflinePlayerIfCached(player)
        if (targetPlayer == null) {
            sender.sendRichMessage("<red>プレイヤー <yellow>$player</yellow> が見つかりません。")
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨が見つかりません。")
            return
        }

        // 対象アカウントを取得
        val account = api.getAccountManager().getAccount(targetPlayer.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>対象のアカウントが見つかりません。")
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
                sender.sendRichMessage("<red>入金に失敗しました: ${error.message}")
            },
            ifRight = { newBalance ->
                val formatted = currency.format(newBalance)
                sender.sendRichMessage(
                    "<green><yellow>${targetPlayer.name}</yellow> に ${currency.format(bigAmount)} を付与しました。残高: ${formatted}",
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

        // 対象プレイヤーを検索
        val targetPlayer = Bukkit.getOfflinePlayerIfCached(player)
        if (targetPlayer == null) {
            sender.sendRichMessage("<red>プレイヤー <yellow>$player</yellow> が見つかりません。")
            return
        }

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendRichMessage("<red>通貨が見つかりません。")
            return
        }

        // 対象アカウントを取得
        val account = api.getAccountManager().getAccount(targetPlayer.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>対象のアカウントが見つかりません。")
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
                sender.sendRichMessage("<red>出金に失敗しました: ${error.message}")
            },
            ifRight = { newBalance ->
                val formatted = currency.format(newBalance)
                sender.sendRichMessage(
                    "<green><yellow>${targetPlayer.name}</yellow> から ${currency.format(bigAmount)} を徴収しました。残高: ${formatted}",
                )
            },
        )
    }
}
