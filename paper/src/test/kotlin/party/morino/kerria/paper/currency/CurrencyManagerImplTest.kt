package party.morino.kerria.paper.currency

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.paper.KerriaTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class CurrencyManagerImplTest : KoinTest {

    private val currencyManager: CurrencyManager by inject()

    @Test
    @DisplayName("Default currency exists on startup")
    fun defaultCurrencyExists() {
        val result = currencyManager.getDefaultCurrency()
        assertTrue(result.isRight())
        assertEquals("JPY", result.getOrNull()!!.name)
    }

    @Test
    @DisplayName("Get currency by nonexistent ID returns error")
    fun getCurrencyNonexistentIdReturnsError() {
        val result = currencyManager.getCurrency(9999)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.CurrencyNotFound)
    }

    @Test
    @DisplayName("Create and retrieve currency")
    fun createAndRetrieveCurrency() {
        val created = currencyManager.createCurrency(
            "EUR", "€", "%amount% EUR", 2, "Euros",
        )
        assertTrue(created.isRight())
        val currency = created.getOrNull()!!
        assertEquals("EUR", currency.name)
        assertEquals("€", currency.symbol)

        val retrieved = currencyManager.getCurrency(currency.id)
        assertTrue(retrieved.isRight())
        assertEquals("EUR", retrieved.getOrNull()!!.name)
    }

    @Test
    @DisplayName("Get all currencies includes default")
    fun getAllCurrenciesIncludesDefault() {
        val result = currencyManager.getAllCurrencies()
        assertTrue(result.isRight())
        val currencies = result.getOrNull()!!
        assertTrue(currencies.any { it.name == "JPY" })
    }

    @Test
    @DisplayName("Get currency by name")
    fun getCurrencyByName() {
        val result = currencyManager.getCurrencyByName("JPY")
        assertTrue(result.isRight())
        assertEquals("JPY", result.getOrNull()!!.name)
    }

    @Test
    @DisplayName("Get currency by nonexistent name returns error")
    fun getCurrencyByNonexistentNameReturnsError() {
        val result = currencyManager.getCurrencyByName("NONEXISTENT")
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.CurrencyNotFound)
    }

    @Test
    @DisplayName("Delete default currency returns error")
    fun deleteDefaultCurrencyReturnsError() {
        val result = currencyManager.deleteCurrency(1)
        assertTrue(result.isLeft())
    }

    @Test
    @DisplayName("Delete nonexistent currency returns error")
    fun deleteNonexistentCurrencyReturnsError() {
        val result = currencyManager.deleteCurrency(9999)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.CurrencyNotFound)
    }

    @Test
    @DisplayName("Create and delete currency")
    fun createAndDeleteCurrency() {
        val created = currencyManager.createCurrency(
            "GBP", "£", "%amount% GBP", 2, "Pounds",
        )
        assertTrue(created.isRight())
        val currencyId = created.getOrNull()!!.id

        val deleted = currencyManager.deleteCurrency(currencyId)
        assertTrue(deleted.isRight())

        val retrieved = currencyManager.getCurrency(currencyId)
        assertTrue(retrieved.isLeft())
    }
}
