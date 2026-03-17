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
import java.time.format.DateTimeFormatter

/**
 * 取引履歴コマンド
 *
 * /kerria log [page]         - 自分の取引履歴
 * /kerria log <player> [page] - 他人の取引履歴（管理者権限が必要）
 */
@Command("kerria")
class LogCommand : KoinComponent {

    private val api: KerriaAPI by inject()

    // 1ページあたりの表示件数
    private val pageSize = 10

    // 日時フォーマット
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

    @Command("log [page]")
    @Permission("kerria.log")
    @Suppress("UnstableApiUsage")
    fun logSelf(stack: CommandSourceStack, @Default("1") page: Int) {
        val sender = stack.sender
        if (sender !is Player) {
            sender.sendRichMessage("<red>このコマンドはプレイヤーのみが使用できます。")
            return
        }

        // 自分のアカウントを取得
        val account = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>アカウントが見つかりません。")
            return
        }

        showLogs(sender, account.name ?: sender.name, account.accountId, page)
    }

    @Command("log player <player> [page]")
    @Permission("kerria.admin.log")
    @Suppress("UnstableApiUsage")
    fun logOther(stack: CommandSourceStack, player: String, @Default("1") page: Int) {
        val sender = stack.sender

        // 対象プレイヤーを検索
        val targetPlayer = Bukkit.getOfflinePlayerIfCached(player)
        if (targetPlayer == null) {
            sender.sendRichMessage("<red>プレイヤー <yellow>$player</yellow> が見つかりません。")
            return
        }

        // 対象のアカウントを取得
        val account = api.getAccountManager().getAccount(targetPlayer.uniqueId).getOrNull() ?: run {
            sender.sendRichMessage("<red>対象のアカウントが見つかりません。")
            return
        }

        showLogs(sender, targetPlayer.name ?: player, account.accountId, page)
    }

    /**
     * 取引ログの表示処理
     */
    private fun showLogs(
        sender: org.bukkit.command.CommandSender,
        playerName: String,
        accountId: java.util.UUID,
        page: Int,
    ) {
        // ページバリデーション
        val safePage = if (page < 1) 1 else page
        val offset = (safePage - 1) * pageSize

        // 取引ログを取得
        val logs = api.getLogManager().getTransactionHistory(accountId, pageSize, offset).getOrNull() ?: run {
            sender.sendRichMessage("<red>取引履歴の取得に失敗しました。")
            return
        }

        if (logs.isEmpty()) {
            sender.sendRichMessage("<yellow>表示する取引履歴がありません。")
            return
        }

        // ヘッダー表示
        sender.sendRichMessage("<gold>===== ${playerName}の取引履歴 - ページ $safePage =====")

        // 各ログを表示
        logs.forEach { log ->
            val time = log.timestamp.format(dateFormatter)
            val currency = api.getCurrencyManager().getCurrency(log.currencyId).getOrNull()
            val amountStr = currency?.format(log.amount) ?: log.amount.toPlainString()

            // 送金方向の表示
            val direction = if (log.fromAccountId == accountId) {
                "<red>-$amountStr"
            } else {
                "<green>+$amountStr"
            }

            // プラグイン名の表示
            val pluginInfo = log.treatePluginName?.let { " <gray>[$it]" } ?: ""

            // メッセージの表示
            val messageInfo = log.message?.let { " <gray>$it" } ?: ""

            sender.sendRichMessage("<gray>$time $direction$pluginInfo$messageInfo")
        }

        // フッター表示
        if (logs.size == pageSize) {
            val nextPage = safePage + 1
            sender.sendRichMessage(
                "<gray>次のページ: <click:run_command:'/kerria log $nextPage'><aqua>[ページ $nextPage]</click>",
            )
        }
    }
}
