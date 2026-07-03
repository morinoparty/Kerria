package party.morino.kerria.paper.integration.placeholder

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.paper.KerriaTest
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(KerriaTest::class)
class KerriaExpansionTest : KoinTest {

    private val accountManager: AccountManager by inject()
    private val currencyManager: CurrencyManager by inject()
    private val economyManager: EconomyManager by inject()

    private val expansion = KerriaExpansion()

    @Test
    @DisplayName("balance placeholder returns formatted default-currency balance")
    fun balanceFormatted() {
        val player = KerriaTest.server.addPlayer()
        val account = accountManager.getOrCreateAccount(player.uniqueId, player.name).getOrNull()!!
        economyManager.deposit(account.accountId, 1, BigDecimal("1000"))

        // コマンド出力と同じ CurrencyFormatter を経由することを確認する
        val balance = accountManager.getBalance(account.accountId, 1).getOrNull()!!
        val expected = currencyManager.getCurrency(1).getOrNull()!!.format(balance)
        assertEquals(expected, expansion.onRequest(player, "balance"))
    }

    @Test
    @DisplayName("balance_raw placeholder returns numeric-only balance")
    fun balanceRaw() {
        val player = KerriaTest.server.addPlayer()
        val account = accountManager.getOrCreateAccount(player.uniqueId, player.name).getOrNull()!!
        economyManager.deposit(account.accountId, 1, BigDecimal("2500"))

        assertEquals("2500.00", expansion.onRequest(player, "balance_raw"))
    }

    @Test
    @DisplayName("currency_default placeholder returns default currency name")
    fun currencyDefault() {
        val player = KerriaTest.server.addPlayer()
        assertEquals("JPY", expansion.onRequest(player, "currency_default"))
    }

    @Test
    @DisplayName("balance falls back to zero for player without account")
    fun balanceFallbackNoAccount() {
        val player = KerriaTest.server.addPlayer()
        // アカウント未作成でも例外を投げず、0 残高としてフォーマットされる
        val expected = currencyManager.getCurrency(1).getOrNull()!!.format(BigDecimal.ZERO)
        assertEquals(expected, expansion.onRequest(player, "balance"))
    }

    @Test
    @DisplayName("null player and unknown params return null")
    fun nullAndUnknownReturnNull() {
        val player = KerriaTest.server.addPlayer()
        assertNull(expansion.onRequest(null, "balance"))
        assertNull(expansion.onRequest(player, "unknown_placeholder"))
    }
}
