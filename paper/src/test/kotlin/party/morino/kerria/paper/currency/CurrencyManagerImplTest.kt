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
    @DisplayName("Created currency formats amount with grouping")
    fun createdCurrencyFormatsWithGrouping() {
        val created = currencyManager.createCurrency(
            "USD", "$", "%symbol%%amount%", 2, "Dollars",
        )
        assertTrue(created.isRight())
        // 桁区切りが適用され、記号が接頭辞として表示されることを確認する
        assertEquals("$1,234.56", created.getOrNull()!!.format(java.math.BigDecimal("1234.56")))
    }

    @Test
    @DisplayName("Update currency persists new values")
    fun updateCurrencyPersistsNewValues() {
        val created = currencyManager.createCurrency(
            "AUD", "$", "%amount% AUD", 2, "Dollars",
        ).getOrNull()!!

        // 記号と小数桁数を変更して更新する
        val updated = currencyManager.updateCurrency(
            created.copy(symbol = "A$", fractionalDigits = 0),
        )
        assertTrue(updated.isRight())

        val retrieved = currencyManager.getCurrency(created.id).getOrNull()!!
        assertEquals("A$", retrieved.symbol)
        assertEquals(0, retrieved.fractionalDigits)
    }

    @Test
    @DisplayName("Update currency to existing name returns error")
    fun updateCurrencyDuplicateNameReturnsError() {
        val created = currencyManager.createCurrency(
            "CHF", "Fr", "%amount% CHF", 2, "Francs",
        ).getOrNull()!!

        // 既存のデフォルト通貨 JPY と同名に変更しようとすると失敗する
        val result = currencyManager.updateCurrency(created.copy(name = "JPY"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.CurrencyAlreadyExists)
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
