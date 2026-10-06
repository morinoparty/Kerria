package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.files.MessageManager
import party.morino.kerria.paper.commands.target.TargetAccountResolver
import party.morino.kerria.paper.message.TransactionMessageFormatter
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
    private val messages: MessageManager by inject()

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
            sender.sendMessage(messages.get("common.player-only"))
            return
        }

        // 自分のアカウントを取得
        val account = api.getAccountManager().getAccount(sender.uniqueId).getOrNull() ?: run {
            sender.sendMessage(messages.get("common.account-not-found"))
            return
        }

        showLogs(sender, account.name ?: sender.name, account.accountId, page)
    }

    @Command("log player <player> [page]")
    @Permission("kerria.admin.log")
    @Suppress("UnstableApiUsage")
    fun logOther(stack: CommandSourceStack, player: String, @Default("1") page: Int) {
        val sender = stack.sender

        // 対象アカウントを解決（キャッシュ非依存でオフラインプレイヤーも解決可能）
        val account = TargetAccountResolver.resolve(api, player) ?: run {
            sender.sendMessage(messages.get("common.player-not-found", "player" to player))
            return
        }

        showLogs(sender, account.name ?: player, account.accountId, page)
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
            sender.sendMessage(messages.get("log.fetch-failed"))
            return
        }

        if (logs.isEmpty()) {
            sender.sendMessage(messages.get("log.empty"))
            return
        }

        // ヘッダー表示
        sender.sendMessage(
            messages.get("log.header", "player" to playerName, "page" to safePage.toString()),
        )

        // 各ログを表示
        logs.forEach { log ->
            val time = log.timestamp.format(dateFormatter)
            val currency = api.getCurrencyManager().getCurrency(log.currencyId).getOrNull()
            val amountStr = currency?.format(log.amount) ?: log.amount.toPlainString()

            // 送金方向で色分けするテンプレートを選ぶ（出金=赤マイナス、入金=緑プラス）
            val key = if (log.fromAccountId == accountId) "log.entry.outgoing" else "log.entry.incoming"

            // プラグイン名は色をテンプレート側に持たせ、値はプレーンで渡す
            val pluginText = log.treatePluginName?.let { "[$it]" } ?: ""
            // メッセージは MiniMessage 形式で保存されているため、装飾系タグのみ解釈して表示する
            val messageComponent = log.message?.let { TransactionMessageFormatter.renderStyleOnly(it) }
                ?: Component.empty()

            sender.sendMessage(
                messages.get(
                    key,
                    mapOf("message" to messageComponent),
                    "time" to time,
                    "amount" to amountStr,
                    "plugin" to pluginText,
                ),
            )
        }

        // フッター表示
        if (logs.size == pageSize) {
            val nextPage = safePage + 1
            // クリックコマンドはコード側で組み立てる（テンプレート内のタグ引数は解決されないため）
            sender.sendMessage(
                messages.get("log.next-page", "page" to nextPage.toString())
                    .clickEvent(ClickEvent.runCommand("/kerria log $nextPage")),
            )
        }
    }
}
