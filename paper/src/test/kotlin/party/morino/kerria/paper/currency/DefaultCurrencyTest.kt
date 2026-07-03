package party.morino.kerria.paper.currency

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.paper.KerriaTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * デフォルト通貨の切り替え（設定の永続化）に関するテスト
 *
 * 共有される config を書き換えるため、各テストは finally で元の設定へ復元する。
 */
@ExtendWith(KerriaTest::class)
class DefaultCurrencyTest : KoinTest {

    private val configManager: ConfigManager by inject()
    private val currencyManager: CurrencyManager by inject()

    @Test
    @DisplayName("Set default currency persists and getDefaultCurrency reflects it")
    fun setDefaultCurrencyPersists() {
        val original = configManager.getConfig()
        try {
            // 新しい通貨を作成し、それをデフォルトに設定する
            val eur = currencyManager.createCurrency("EURD", "€", "%amount% EURD", 2, "Euros").getOrNull()!!
            val newCurrency = original.economy.currency.copy(
                id = eur.id,
                name = eur.name,
                symbol = eur.symbol,
                plural = eur.plural,
                format = eur.format,
                fractionalDigits = eur.fractionalDigits,
            )
            val newConfig = original.copy(economy = original.economy.copy(currency = newCurrency))

            val result = configManager.updateConfig(newConfig)
            assertTrue(result.isRight())
            // デフォルト通貨が切り替わっていることを確認
            assertEquals("EURD", currencyManager.getDefaultCurrency().getOrNull()!!.name)
        } finally {
            // 他テストへ影響しないよう元の設定へ戻す
            configManager.updateConfig(original)
        }
    }

    @Test
    @DisplayName("updateConfig round-trips through disk")
    fun updateConfigRoundTrips() {
        val original = configManager.getConfig()
        try {
            // id を変更して保存し、リロードで永続化を確認する
            val changed = original.copy(
                economy = original.economy.copy(currency = original.economy.currency.copy(id = 999)),
            )
            assertTrue(configManager.updateConfig(changed).isRight())
            assertTrue(configManager.reloadConfig().isRight())
            assertEquals(999, configManager.getConfig().economy.currency.id)
        } finally {
            configManager.updateConfig(original)
        }
    }
}
