package party.morino.kerria.common.placeholder

import io.github.miniplaceholders.api.Expansion
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identified
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import party.morino.kerria.api.currency.Currency
import java.math.BigDecimal
import java.util.UUID

/**
 * MiniPlaceholders に登録する Kerria のプレースホルダー
 *
 * Paper と Velocity で共通の定義を使い、残高の取得元だけを [PlaceholderBalanceSource] で差し替える。
 * 提供するプレースホルダー（引数はいずれも省略可能）:
 * - `<kerria_balance[:通貨][:桁数]>` 残高（通貨フォーマット適用）
 * - `<kerria_balance_raw[:通貨][:桁数]>` 残高（数値のみ）
 * - `<kerria_currency_default>` デフォルト通貨の名前
 *
 * 通貨には通貨ID・通貨名・`default` を指定できる。桁数を指定すると、小数点以下をその桁数で切り捨てて表示する。
 *
 * MiniPlaceholders が無い環境ではこのクラスを読み込まないよう、呼び出し側でプラグインの有無を確認すること。
 */
object KerriaPlaceholders {
    // プレースホルダーの名前空間（<kerria_xxx>）
    private const val NAMESPACE = "kerria"

    // 通貨の引数でデフォルト通貨を明示する値
    private const val DEFAULT_CURRENCY_ARGUMENT = "default"

    /**
     * プレースホルダーを定義したエクスパンションを作成する
     *
     * @param source 残高・通貨の取得元
     * @param version エクスパンションのバージョン（プラグインのバージョン）
     * @return 未登録のエクスパンション（[Expansion.register] で登録する）
     */
    fun create(source: PlaceholderBalanceSource, version: String): Expansion =
        Expansion.builder(NAMESPACE)
            .author("morinoparty")
            .version(version)
            .audiencePlaceholder("balance") { audience, queue, _ ->
                text(resolveBalance(source, audience, queue, BalancePlaceholderFormatter::formatted))
            }
            .audiencePlaceholder("balance_raw") { audience, queue, _ ->
                text(resolveBalance(source, audience, queue, BalancePlaceholderFormatter::raw))
            }
            .globalPlaceholder("currency_default") { _, _ ->
                text(source.defaultCurrency()?.name ?: "")
            }
            .build()

    /**
     * 引数を読み取り、対象プレイヤーの残高を表示文字列にする
     *
     * プレイヤー以外（コンソールなど）や、通貨が見つからない場合は空文字にする。
     * アカウントが無いプレイヤーの残高は 0 として扱う。
     */
    private fun resolveBalance(
        source: PlaceholderBalanceSource,
        audience: Audience,
        queue: ArgumentQueue,
        render: (Currency, BigDecimal, Int?) -> String,
    ): String {
        val playerUniqueId = playerUniqueId(audience) ?: return ""
        // 1 つ目の引数は通貨、2 つ目の引数は桁数
        val currencyArgument = if (queue.hasNext()) queue.pop().value() else null
        val digits = BalancePlaceholderFormatter.parseDigits(if (queue.hasNext()) queue.pop().value() else null)
        val currency = resolveCurrency(source, currencyArgument) ?: return ""
        val balance = source.balance(playerUniqueId, currency.id) ?: BigDecimal.ZERO
        return render(currency, balance, digits)
    }

    /**
     * 通貨の引数から通貨を解決する（省略・`default` ならデフォルト通貨、数値なら ID、それ以外は名前）
     */
    private fun resolveCurrency(source: PlaceholderBalanceSource, argument: String?): Currency? {
        if (argument == null || argument.equals(DEFAULT_CURRENCY_ARGUMENT, ignoreCase = true)) {
            return source.defaultCurrency()
        }
        return argument.toIntOrNull()?.let(source::currencyById) ?: source.currencyByName(argument)
    }

    /**
     * Audience からプレイヤーの UUID を取得する
     *
     * Paper / Velocity のプレイヤーは [Identified] を実装しているためそれを優先し、
     * それ以外は Audience のポインター（[Identity.UUID]）から取得する。
     */
    private fun playerUniqueId(audience: Audience): UUID? =
        (audience as? Identified)?.identity()?.uuid() ?: audience.get(Identity.UUID).orElse(null)

    /** 文字列をプレースホルダーの結果（挿入されるテキスト）にする */
    private fun text(value: String): Tag = Tag.selfClosingInserting(Component.text(value))
}
