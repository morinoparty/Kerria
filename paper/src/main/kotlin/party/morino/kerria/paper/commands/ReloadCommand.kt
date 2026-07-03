package party.morino.kerria.paper.commands

import arrow.core.flatMap
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.files.MessageManager

/**
 * プラグイン再読み込みコマンド
 *
 * /kerria reload - 設定ファイルとメッセージファイルをディスクから再読み込みする
 */
@Command("kerria")
class ReloadCommand : KoinComponent {

    // ConfigManager は KerriaAPI に公開されていないため直接注入する
    private val configManager: ConfigManager by inject()
    private val messages: MessageManager by inject()

    @Command("reload")
    @Permission("kerria.admin.reload")
    @Suppress("UnstableApiUsage")
    fun reload(stack: CommandSourceStack) {
        val sender = stack.sender

        // 設定を再読み込みし、成功したらメッセージも再読み込みする
        configManager.reloadConfig()
            .flatMap { messages.reloadMessages() }
            .fold(
                ifLeft = { error ->
                    sender.sendRichMessage(messages.get("reload.failed", "error" to (error.message ?: "")))
                },
                ifRight = {
                    sender.sendRichMessage(messages.get("reload.success"))
                },
            )
    }
}
