package party.morino.kerria.paper.currency

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.paper.KerriaTest
import party.morino.kerria.paper.economy.VaultEconomy
import java.io.File
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
    @DisplayName("Default currency id selects currency details from the database")
    fun setDefaultCurrencyPersists() {
        val original = configManager.getConfig()
        try {
            // 新しい通貨を作成し、設定にはIDだけを書き込む
            val eur = currencyManager.createCurrency("EURD", "€", "%amount% EURD", 3, "Euros").getOrNull()!!
            val newConfig = original.copy(economy = original.economy.copy(defaultCurrencyId = eur.id))
            assertTrue(configManager.updateConfig(newConfig).isRight())

            // 名前・複数形・桁数はDBの通貨から引かれる
            assertEquals("EURD", currencyManager.getDefaultCurrency().getOrNull()!!.name)
            val vault = VaultEconomy()
            assertEquals("Euros", vault.currencyNamePlural())
            assertEquals("EURD", vault.currencyNameSingular())
            assertEquals(3, vault.fractionalDigits())
        } finally {
            // 他テストへ影響しないよう元の設定へ戻す
            configManager.updateConfig(original)
        }
    }

    @Test
    @DisplayName("updateConfig round-trips through the HOCON file and keeps comments")
    fun updateConfigRoundTrips() {
        val original = configManager.getConfig()
        try {
            // id を変更して保存し、リロードで永続化を確認する
            val changed = original.copy(economy = original.economy.copy(defaultCurrencyId = 999))
            assertTrue(configManager.updateConfig(changed).isRight())
            assertTrue(configManager.reloadConfig().isRight())
            assertEquals(999, configManager.getConfig().economy.defaultCurrencyId)

            // ファイルは kebab-case の HOCON で、既定ファイルのコメントが残っている
            val text = File(KerriaTest.plugin.dataFolder, "config.conf").readText()
            assertTrue(text.contains("default-currency-id"), text)
            assertTrue(text.contains("デフォルト通貨のID"), text)
        } finally {
            configManager.updateConfig(original)
        }
    }
}
