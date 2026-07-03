package party.morino.kerria.paper.commands

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.files.MessageManager

/**
 * 残高ランキングコマンド
 *
 * /kerria top [currencyId] [page]
 */
@Command("kerria")
class TopCommand : KoinComponent {

    private val api: KerriaAPI by inject()
    private val messages: MessageManager by inject()

    // 1ページあたりの表示件数
    private val pageSize = 10

    @Command("top [currencyId] [page]")
    @Permission("kerria.top")
    @Suppress("UnstableApiUsage")
    fun top(
        stack: CommandSourceStack,
        @Default("1") currencyId: Int,
        @Default("1") page: Int,
    ) {
        val sender = stack.sender

        // 通貨を取得
        val currency = api.getCurrencyManager().getCurrency(currencyId).getOrNull() ?: run {
            sender.sendRichMessage(messages.get("common.currency-not-found"))
            return
        }

        // ページバリデーション
        val safePage = if (page < 1) 1 else page
        val offset = (safePage - 1) * pageSize

        // ランキングデータを取得
        val entries = api.getAccountManager().getTopBalances(currencyId, pageSize, offset).getOrNull() ?: run {
            sender.sendRichMessage(messages.get("top.fetch-failed"))
            return
        }

        if (entries.isEmpty()) {
            sender.sendRichMessage(messages.get("top.empty"))
            return
        }

        // ヘッダー表示
        sender.sendRichMessage(
            messages.get("top.header", "currency" to currency.name, "page" to safePage.toString()),
        )

        // ランキング表示
        entries.forEachIndexed { index, (account, balance) ->
            val rank = offset + index + 1
            val name = account.name ?: "Unknown"
            val formatted = currency.format(balance)
            sender.sendRichMessage(
                messages.get(
                    "top.entry",
                    "rank" to rank.toString(),
                    "name" to name,
                    "amount" to formatted,
                ),
            )
        }

        // フッター表示（次ページへのヒント）
        if (entries.size == pageSize) {
            val nextPage = safePage + 1
            sender.sendRichMessage(
                messages.get(
                    "top.next-page",
                    "currencyId" to currencyId.toString(),
                    "nextPage" to nextPage.toString(),
                ),
            )
        }
    }
}
