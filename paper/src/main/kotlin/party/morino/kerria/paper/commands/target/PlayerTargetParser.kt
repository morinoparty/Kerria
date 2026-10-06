package party.morino.kerria.paper.commands.target

import org.bukkit.Bukkit
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

/**
 * [PlayerTarget] を解析する Cloud のパーサー
 *
 * 空白までの1単語をそのまま受け取る。Brigadier 上では Paper のプレイヤーセレクタ引数に
 * 対応付けて登録するため（[party.morino.kerria.paper.Kerria] 参照）、クライアントでは
 * セレクタやプレイヤー名の補完が表示され、`@` を含む入力も Brigadier の解析を通過できる。
 *
 * @param C コマンド送信者の型
 */
class PlayerTargetParser<C : Any> :
    ArgumentParser<C, PlayerTarget>,
    BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<PlayerTarget> {
        // セレクタの引数部分（例: @a[distance=..10]）も含めて空白までを1つの入力として読む
        val input = commandInput.readString()
        if (input.isEmpty()) {
            return ArgumentParseResult.failure(IllegalArgumentException("No player or selector was given"))
        }
        return ArgumentParseResult.success(PlayerTarget(input))
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        // Brigadier を経由しない環境向けの補完（セレクタとオンラインプレイヤー名）
        return SELECTORS + Bukkit.getOnlinePlayers().map { it.name }
    }

    companion object {
        // 補完に表示するプレイヤー向けのセレクタ
        private val SELECTORS = listOf("@a", "@p", "@r", "@s")
    }
}
