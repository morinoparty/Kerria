package party.morino.kerria.velocity.placeholder

import party.morino.kerria.api.currency.Currency
import party.morino.kerria.common.placeholder.PlaceholderBalanceSource
import party.morino.kerria.velocity.economy.CrossServerEconomy
import java.math.BigDecimal
import java.util.UUID

/**
 * Velocity 用のプレースホルダーの残高取得元
 *
 * 共有データベースから直接取得する。デフォルト通貨はプロキシの設定（default-currency-id）に従う。
 *
 * @property economy 共有データベースへのアクセス
 * @property defaultCurrencyId プロキシのデフォルト通貨ID
 */
class VelocityPlaceholderBalanceSource(
    private val economy: CrossServerEconomy,
    private val defaultCurrencyId: Int,
) : PlaceholderBalanceSource {

    override fun defaultCurrency(): Currency? = currencyById(defaultCurrencyId)

    override fun currencyById(id: Int): Currency? = runCatching { economy.currency(id) }.getOrNull()

    override fun currencyByName(name: String): Currency? = runCatching { economy.currencyByName(name) }.getOrNull()

    // DB 接続の失敗などでプレースホルダーの描画を止めないよう、例外は null に畳み込む
    override fun balance(playerUniqueId: UUID, currencyId: Int): BigDecimal? =
        runCatching { economy.balanceOf(playerUniqueId, currencyId) }.getOrNull()
}
