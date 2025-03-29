package party.morino.kerria.economy

import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.Kerria
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.files.ConfigManager
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.runBlocking
@SuppressWarnings("deprecation")
class VaultEconomy : AbstractEconomy(), KoinComponent {
    private val plugin: Kerria by inject()

    private val accountManager: AccountManager by inject()
    private val configManager: ConfigManager by inject()

    override fun isEnabled(): Boolean {
        return plugin.isEnabled
    }

    override fun getName(): String {
        return plugin.pluginMeta.name
    }

    override fun hasBankSupport(): Boolean {
        return true
    }

    override fun fractionalDigits(): Int {
        return configManager.getConfig().economy.fractionalDigits
    }

    override fun format(amount: Double): String {
        return BigDecimal.valueOf(amount).setScale(fractionalDigits(), RoundingMode.HALF_UP).toPlainString();
    }

    override fun currencyNamePlural(): String {
        return currencyNameSingular()
    }

    override fun currencyNameSingular(): String {
        return configManager.getConfig().economy.currency.symbol
    }

    override fun hasAccount(playerName: String): Boolean {
        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(playerName) ?: return false
        accountManager.getAccount(offlinePlayer).isLeft {
            return false
        }
        return true
    }

    override fun hasAccount(playerName: String, worldName: String?): Boolean {
        return hasAccount(playerName)
    }

    override fun getBalance(playerName: String): Double {
        val offlinePlayer: OfflinePlayer = Bukkit.getOfflinePlayerIfCached(playerName) ?: return 0.0
        return getBalance(offlinePlayer)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        val account = accountManager.getAccount(player).getOrNull() ?: return 0.0
        return runBlocking { account.getBalance() }.setScale(fractionalDigits(), RoundingMode.HALF_UP).toDouble()
    }

    override fun getBalance(player: OfflinePlayer, world: String?): Double {
        return getBalance(player)
    }

    override fun getBalance(playerName: String, world: String): Double {
        return getBalance(playerName)
    }

    override fun has(playerName: String, amount: Double): Boolean {
        return (getBalance(playerName) >= amount)
    }

    override fun has(playerName: String, worldName: String?, amount: Double): Boolean {
        return has(playerName,amount)
    }

    override fun withdrawPlayer(playerName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun withdrawPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun depositPlayer(playerName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun depositPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun createBank(name: String?, player: String?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun deleteBank(name: String): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankBalance(name: String): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankHas(name: String, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankDeposit(name: String, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun isBankOwner(name: String, playerName: String?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun isBankMember(name: String, playerName: String): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun getBanks(): List<String> {
        TODO("Not yet implemented")
    }

    override fun createPlayerAccount(playerName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun createPlayerAccount(playerName: String, worldName: String?): Boolean {
        return createPlayerAccount(playerName)
    }
}