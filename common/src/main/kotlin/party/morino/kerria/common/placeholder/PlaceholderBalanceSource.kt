package party.morino.kerria.common.placeholder

import party.morino.kerria.api.currency.Currency
import java.math.BigDecimal
import java.util.UUID

/**
 * プレースホルダーが参照する残高・通貨の取得元
 *
 * Paper と Velocity で残高の取得方法が異なるため、プラットフォームごとに実装する（Strategy パターン）。
 * プレースホルダーの描画中に呼ばれるため、実装は例外を投げずに null を返すこと。
 */
interface PlaceholderBalanceSource {
    /**
     * デフォルト通貨を取得する
     *
     * @return デフォルト通貨。取得できなければ null
     */
    fun defaultCurrency(): Currency?

    /**
     * ID で通貨を取得する
     *
     * @param id 通貨ID
     * @return 通貨。存在しなければ null
     */
    fun currencyById(id: Int): Currency?

    /**
     * 名前で通貨を取得する
     *
     * @param name 通貨名
     * @return 通貨。存在しなければ null
     */
    fun currencyByName(name: String): Currency?

    /**
     * プレイヤーの残高を取得する
     *
     * @param playerUniqueId プレイヤーの Minecraft UUID
     * @param currencyId 通貨ID
     * @return 残高。アカウントが無い・取得に失敗した場合は null
     */
    fun balance(playerUniqueId: UUID, currencyId: Int): BigDecimal?
}
