package party.morino.kerria.paper.integration.placeholder

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.common.placeholder.PlaceholderBalanceSource
import java.math.BigDecimal
import java.util.UUID

/**
 * Paper 用のプレースホルダーの残高取得元
 *
 * Kerria の各マネージャーから取得し、[arrow.core.Either] の失敗は null に畳み込む。
 */
class PaperPlaceholderBalanceSource : PlaceholderBalanceSource, KoinComponent {
    private val accountManager: AccountManager by inject()
    private val currencyManager: CurrencyManager by inject()

    override fun defaultCurrency(): Currency? = currencyManager.getDefaultCurrency().getOrNull()

    override fun currencyById(id: Int): Currency? = currencyManager.getCurrency(id).getOrNull()

    override fun currencyByName(name: String): Currency? = currencyManager.getCurrencyByName(name).getOrNull()

    override fun balance(playerUniqueId: UUID, currencyId: Int): BigDecimal? {
        // アカウントが無いプレイヤーは null（プレースホルダー側で 0 として表示する）
        val account = accountManager.getAccount(playerUniqueId).getOrNull() ?: return null
        return accountManager.getBalance(account.accountId, currencyId).getOrNull()
    }
}
