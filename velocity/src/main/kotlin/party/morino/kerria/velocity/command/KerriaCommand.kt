package party.morino.kerria.velocity.command

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import party.morino.kerria.velocity.economy.CrossServerEconomy
import java.math.BigDecimal

/**
 * Velocity 側の `/kerria` コマンド
 *
 * `balance` と `pay` のサブコマンドを提供し、共有DBを通じてクロスサーバーで動作する。
 */
class KerriaCommand(
    private val proxy: ProxyServer,
    private val economy: CrossServerEconomy,
    private val currencyId: Int,
) : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        val args = invocation.arguments()
        when (args.getOrNull(0)?.lowercase()) {
            "balance" -> handleBalance(source)
            "pay" -> handlePay(source, args)
            else -> source.sendMessage(text("Usage: /kerria <balance|pay>", NamedTextColor.GRAY))
        }
    }

    private fun handleBalance(source: com.velocitypowered.api.command.CommandSource) {
        if (source !is Player) {
            source.sendMessage(text("This command can only be used by players.", NamedTextColor.RED))
            return
        }
        val balance = economy.balanceOf(source.uniqueId, currencyId) ?: BigDecimal.ZERO
        source.sendMessage(text("Your balance: ${format(balance)}", NamedTextColor.GREEN))
    }

    private fun handlePay(source: com.velocitypowered.api.command.CommandSource, args: Array<String>) {
        if (source !is Player) {
            source.sendMessage(text("This command can only be used by players.", NamedTextColor.RED))
            return
        }
        val targetName = args.getOrNull(1)
        val amount = args.getOrNull(2)?.toBigDecimalOrNull()
        if (targetName == null || amount == null) {
            source.sendMessage(text("Usage: /kerria pay <player> <amount>", NamedTextColor.GRAY))
            return
        }
        if (amount <= BigDecimal.ZERO) {
            source.sendMessage(text("The amount must be a positive number.", NamedTextColor.RED))
            return
        }

        val target = economy.resolveAccountByName(targetName)
        val targetUuid = target?.playerUniqueId
        if (targetUuid == null) {
            source.sendMessage(text("Player $targetName was not found.", NamedTextColor.RED))
            return
        }
        if (targetUuid == source.uniqueId) {
            source.sendMessage(text("You cannot send money to yourself.", NamedTextColor.RED))
            return
        }

        when (val result = economy.transfer(source.uniqueId, targetUuid, amount, currencyId)) {
            is CrossServerEconomy.TransferResult.Success -> {
                val formatted = format(amount)
                source.sendMessage(text("Sent $formatted to ${target.name ?: targetName}.", NamedTextColor.GREEN))
                // 送金先がプロキシ上にオンラインなら通知する
                proxy.getPlayer(targetUuid).ifPresent { online ->
                    online.sendMessage(text("Received $formatted from ${source.username}.", NamedTextColor.GREEN))
                }
            }
            is CrossServerEconomy.TransferResult.InsufficientFunds ->
                source.sendMessage(text("You do not have enough funds.", NamedTextColor.RED))
            is CrossServerEconomy.TransferResult.AccountNotFound ->
                source.sendMessage(text("Account not found.", NamedTextColor.RED))
            is CrossServerEconomy.TransferResult.Error ->
                source.sendMessage(text("Payment failed: ${result.message}", NamedTextColor.RED))
        }
    }

    /** 金額を通貨フォーマットで文字列化する（通貨情報が無ければ素の数値） */
    private fun format(amount: BigDecimal): String =
        economy.currency(currencyId)?.format(amount) ?: amount.toPlainString()

    private fun text(content: String, color: NamedTextColor): Component =
        Component.text(content, color)
}
