package party.morino.kerria.paper.integration.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.paper.Kerria
import java.math.BigDecimal
import java.util.UUID

/**
 * PlaceholderAPI 連携用の Expansion
 *
 * Kerria の残高・通貨情報をプレースホルダとして公開する。
 * スコアボード・タブ・チャットなど PlaceholderAPI を消費するプラグインから利用できる。
 *
 * 提供するプレースホルダ:
 * - `%kerria_balance%`           デフォルト通貨の残高（通貨フォーマット適用）
 * - `%kerria_balance_raw%`       デフォルト通貨の残高（数値のみ）
 * - `%kerria_balance_<id>%`      指定通貨IDの残高（通貨フォーマット適用）
 * - `%kerria_balance_raw_<id>%`  指定通貨IDの残高（数値のみ）
 * - `%kerria_currency_default%`  デフォルト通貨の名前
 *
 * すべての取得は [arrow.core.Either] を安全なフォールバックへ畳み込むため、
 * 消費側プラグインの描画処理に例外を投げることはない。
 */
class KerriaExpansion : PlaceholderExpansion(), KoinComponent {

    // 依存は Koin のフィールドインジェクションで取得する
    private val plugin: Kerria by inject()
    private val accountManager: AccountManager by inject()
    private val currencyManager: CurrencyManager by inject()

    override fun getIdentifier(): String = "kerria"

    override fun getAuthor(): String =
        plugin.pluginMeta.authors.joinToString().ifEmpty { "morinoparty" }

    override fun getVersion(): String = plugin.pluginMeta.version

    // PlaceholderAPI のリロードをまたいで登録を維持する
    override fun persist(): Boolean = true

    /**
     * プレースホルダのリクエストを解決する
     *
     * @param player 対象プレイヤー（未指定なら null）
     * @param params `kerria_` プレフィックスを除いたパラメータ
     * @return 解決した文字列。対応しないパラメータは null（未処理として扱われる）
     */
    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val uuid = player?.uniqueId ?: return null
        return when {
            // デフォルト通貨の名前
            params == "currency_default" ->
                currencyManager.getDefaultCurrency().getOrNull()?.name ?: ""

            // デフォルト通貨の残高（数値のみ）
            params == "balance_raw" -> rawBalance(uuid, defaultCurrencyId())

            // デフォルト通貨の残高（フォーマット適用）
            params == "balance" -> formattedBalance(uuid, defaultCurrencyId())

            // 指定通貨IDの残高（数値のみ）
            params.startsWith("balance_raw_") -> {
                val id = params.removePrefix("balance_raw_").toIntOrNull() ?: return null
                rawBalance(uuid, id)
            }

            // 指定通貨IDの残高（フォーマット適用）
            params.startsWith("balance_") -> {
                val id = params.removePrefix("balance_").toIntOrNull() ?: return null
                formattedBalance(uuid, id)
            }

            else -> null
        }
    }

    /** デフォルト通貨のIDを返す（取得失敗時は 1 にフォールバック） */
    private fun defaultCurrencyId(): Int =
        currencyManager.getDefaultCurrency().getOrNull()?.id ?: 1

    /** 指定通貨の残高を通貨フォーマットで返す（コマンド出力と一致） */
    private fun formattedBalance(uuid: UUID, currencyId: Int): String {
        val currency = currencyManager.getCurrency(currencyId).getOrNull() ?: return ""
        return currency.format(resolveBalance(uuid, currencyId))
    }

    /** 指定通貨の残高を数値のみの文字列で返す */
    private fun rawBalance(uuid: UUID, currencyId: Int): String {
        val currency = currencyManager.getCurrency(currencyId).getOrNull() ?: return ""
        return currency.round(resolveBalance(uuid, currencyId)).toPlainString()
    }

    /** 残高を取得する（アカウント未作成・取得失敗時は 0 を返す） */
    private fun resolveBalance(uuid: UUID, currencyId: Int): BigDecimal {
        val account = accountManager.getAccount(uuid).getOrNull() ?: return BigDecimal.ZERO
        return accountManager.getBalance(account.accountId, currencyId).getOrNull() ?: BigDecimal.ZERO
    }
}
