package party.morino.kerria.paper.economy

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.economy.ExchangeRateManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.paper.KerriaTest
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class ExchangeRateManagerImplTest : KoinTest {

    private val exchangeRateManager: ExchangeRateManager by inject()
    private val currencyManager: CurrencyManager by inject()
    private val economyManager: EconomyManager by inject()
    private val accountManager: AccountManager by inject()

    private fun createAccount(name: String): UUID {
        val uuid = UUID.randomUUID()
        return accountManager.getOrCreateAccount(uuid, name).getOrNull()!!.accountId
    }

    private fun createSecondCurrency(): Int {
        val currency = currencyManager.createCurrency(
            "USD", "$", "%amount% USD", 2, "Dollars",
        ).getOrNull()!!
        return currency.id
    }

    @Test
    @DisplayName("Set and get exchange rate")
    fun setAndGetRate() {
        val usdId = createSecondCurrency()
        exchangeRateManager.setRate(1, usdId, BigDecimal("0.0067"))

        val result = exchangeRateManager.getRate(1, usdId)
        assertTrue(result.isRight())
        assertEquals(BigDecimal("0.00670000"), result.getOrNull())
    }

    @Test
    @DisplayName("Set rate with zero value returns error")
    fun setRateZeroReturnsError() {
        val usdId = createSecondCurrency()
        val result = exchangeRateManager.setRate(1, usdId, BigDecimal.ZERO)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InvalidAmount)
    }

    @Test
    @DisplayName("Set rate for same currency returns error")
    fun setRateSameCurrencyReturnsError() {
        val result = exchangeRateManager.setRate(1, 1, BigDecimal("1.0"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InvalidAmount)
    }

    @Test
    @DisplayName("Get rate for nonexistent pair returns error")
    fun getRateNonexistentPairReturnsError() {
        val result = exchangeRateManager.getRate(1, 9999)
        assertTrue(result.isLeft())
    }

    @Test
    @DisplayName("Convert currency between accounts")
    fun convertCurrency() {
        val usdId = createSecondCurrency()
        val accountId = createAccount("Converter")

        // JPY を 10000 入金、レートを 0.01 (1 JPY = 0.01 USD) に設定
        economyManager.deposit(accountId, 1, BigDecimal("10000"))
        exchangeRateManager.setRate(1, usdId, BigDecimal("0.01"))

        val result = exchangeRateManager.convert(accountId, 1, usdId, BigDecimal("5000"))
        assertTrue(result.isRight())
        assertEquals(BigDecimal("50.00"), result.getOrNull())

        // 残高を確認
        val jpyBalance = accountManager.getBalance(accountId, 1).getOrNull()!!
        val usdBalance = accountManager.getBalance(accountId, usdId).getOrNull()!!
        assertEquals(BigDecimal("5000.0000"), jpyBalance)
        assertEquals(BigDecimal("50.0000"), usdBalance)
    }

    @Test
    @DisplayName("Convert with insufficient balance returns error")
    fun convertInsufficientBalanceReturnsError() {
        val usdId = createSecondCurrency()
        val accountId = createAccount("PoorConverter")
        exchangeRateManager.setRate(1, usdId, BigDecimal("0.01"))

        val result = exchangeRateManager.convert(accountId, 1, usdId, BigDecimal("5000"))
        assertTrue(result.isLeft())
    }

    @Test
    @DisplayName("Convert to same currency returns error")
    fun convertSameCurrencyReturnsError() {
        val accountId = createAccount("SameConvert")
        val result = exchangeRateManager.convert(accountId, 1, 1, BigDecimal("100"))
        assertTrue(result.isLeft())
    }
}
