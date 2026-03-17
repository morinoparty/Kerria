package party.morino.kerria.paper.economy

import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.paper.Kerria
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Vault Economy API のブリッジ実装
 *
 * EconomyManager に委譲して Vault API を実現する。
 */
@Suppress("deprecation")
class VaultEconomy : AbstractEconomy(), KoinComponent {
    private val plugin: Kerria by inject()
    private val accountManager: AccountManager by inject()
    private val economyManager: EconomyManager by inject()
    private val currencyManager: CurrencyManager by inject()
    private val configManager: ConfigManager by inject()

    /** デフォルト通貨IDを取得するヘルパー */
    private val defaultCurrencyId: Int
        get() = configManager.getConfig().economy.currency.id

    override fun isEnabled(): Boolean = plugin.isEnabled

    override fun getName(): String = plugin.pluginMeta.name

    // 銀行機能は未対応
    override fun hasBankSupport(): Boolean = false

    override fun fractionalDigits(): Int = configManager.getConfig().economy.fractionalDigits

    override fun format(amount: Double): String {
        val currency = currencyManager.getDefaultCurrency().getOrNull()
            ?: return BigDecimal.valueOf(amount).setScale(fractionalDigits(), RoundingMode.HALF_UP).toPlainString()
        return currency.format(BigDecimal.valueOf(amount))
    }

    override fun currencyNamePlural(): String = configManager.getConfig().economy.currency.plural

    override fun currencyNameSingular(): String = configManager.getConfig().economy.currency.name

    // --- アカウント存在確認 ---

    override fun hasAccount(playerName: String): Boolean {
        val player = Bukkit.getOfflinePlayerIfCached(playerName) ?: return false
        return accountManager.getAccount(player.uniqueId).isRight()
    }

    override fun hasAccount(playerName: String, worldName: String?): Boolean = hasAccount(playerName)

    // --- 残高取得 ---

    override fun getBalance(playerName: String): Double {
        val player = Bukkit.getOfflinePlayerIfCached(playerName) ?: return 0.0
        return getBalanceByUuid(player.uniqueId)
    }

    override fun getBalance(playerName: String, world: String): Double = getBalance(playerName)

    override fun getBalance(player: org.bukkit.OfflinePlayer): Double = getBalanceByUuid(player.uniqueId)

    override fun getBalance(player: org.bukkit.OfflinePlayer, world: String?): Double = getBalance(player)

    // --- 残高チェック ---

    override fun has(playerName: String, amount: Double): Boolean = getBalance(playerName) >= amount

    override fun has(playerName: String, worldName: String?, amount: Double): Boolean = has(playerName, amount)

    // --- 出金 ---

    override fun withdrawPlayer(playerName: String?, amount: Double): EconomyResponse {
        if (playerName == null) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player name is null")
        }
        val player = Bukkit.getOfflinePlayerIfCached(playerName)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player not found")
        return withdrawByUuid(player.uniqueId, amount)
    }

    override fun withdrawPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse =
        withdrawPlayer(playerName, amount)

    override fun withdrawPlayer(player: org.bukkit.OfflinePlayer, amount: Double): EconomyResponse =
        withdrawByUuid(player.uniqueId, amount)

    override fun withdrawPlayer(player: org.bukkit.OfflinePlayer, worldName: String?, amount: Double): EconomyResponse =
        withdrawPlayer(player, amount)

    // --- 入金 ---

    override fun depositPlayer(playerName: String?, amount: Double): EconomyResponse {
        if (playerName == null) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player name is null")
        }
        val player = Bukkit.getOfflinePlayerIfCached(playerName)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player not found")
        return depositByUuid(player.uniqueId, amount)
    }

    override fun depositPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse =
        depositPlayer(playerName, amount)

    override fun depositPlayer(player: org.bukkit.OfflinePlayer, amount: Double): EconomyResponse =
        depositByUuid(player.uniqueId, amount)

    override fun depositPlayer(player: org.bukkit.OfflinePlayer, worldName: String?, amount: Double): EconomyResponse =
        depositPlayer(player, amount)

    // --- アカウント作成 ---

    override fun createPlayerAccount(playerName: String): Boolean {
        val player = Bukkit.getOfflinePlayerIfCached(playerName) ?: return false
        return accountManager.getOrCreateAccount(player.uniqueId, playerName).isRight()
    }

    override fun createPlayerAccount(playerName: String, worldName: String?): Boolean =
        createPlayerAccount(playerName)

    override fun createPlayerAccount(player: org.bukkit.OfflinePlayer): Boolean {
        val name = player.name ?: return false
        return accountManager.getOrCreateAccount(player.uniqueId, name).isRight()
    }

    override fun createPlayerAccount(player: org.bukkit.OfflinePlayer, worldName: String?): Boolean =
        createPlayerAccount(player)

    // --- 銀行機能（未対応） ---

    override fun createBank(name: String?, player: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank not supported")

    override fun deleteBank(name: String): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank not supported")

    override fun bankBalance(name: String): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank not supported")

    override fun bankHas(name: String, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank not supported")

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank not supported")

    override fun bankDeposit(name: String, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank not supported")

    override fun isBankOwner(name: String, playerName: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank not supported")

    override fun isBankMember(name: String, playerName: String): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank not supported")

    override fun getBanks(): List<String> = emptyList()

    // --- 内部ヘルパーメソッド ---

    /** UUID からアカウントの残高を取得する */
    private fun getBalanceByUuid(uuid: java.util.UUID): Double {
        val account = accountManager.getAccount(uuid).getOrNull() ?: return 0.0
        return accountManager.getBalance(account.accountId, defaultCurrencyId)
            .getOrNull()
            ?.setScale(fractionalDigits(), RoundingMode.HALF_UP)
            ?.toDouble()
            ?: 0.0
    }

    /** UUID から出金する */
    private fun withdrawByUuid(uuid: java.util.UUID, amount: Double): EconomyResponse {
        val account = accountManager.getAccount(uuid).getOrNull()
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Account not found")

        // 呼び出し元プラグインを自動検出（特定できない場合は "Vault" をフォールバック）
        val callerPlugin = CallerPluginIdentifier.identify() ?: "Vault"

        return economyManager.withdraw(
            account.accountId,
            defaultCurrencyId,
            BigDecimal.valueOf(amount),
            treatePluginName = callerPlugin,
        ).fold(
            ifLeft = { error ->
                val balance = getBalanceByUuid(uuid)
                EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, error.message)
            },
            ifRight = { newBalance ->
                EconomyResponse(amount, newBalance.toDouble(), EconomyResponse.ResponseType.SUCCESS, null)
            },
        )
    }

    /** UUID から入金する */
    private fun depositByUuid(uuid: java.util.UUID, amount: Double): EconomyResponse {
        val account = accountManager.getAccount(uuid).getOrNull()
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Account not found")

        // 呼び出し元プラグインを自動検出（特定できない場合は "Vault" をフォールバック）
        val callerPlugin = CallerPluginIdentifier.identify() ?: "Vault"

        return economyManager.deposit(
            account.accountId,
            defaultCurrencyId,
            BigDecimal.valueOf(amount),
            treatePluginName = callerPlugin,
        ).fold(
            ifLeft = { error ->
                val balance = getBalanceByUuid(uuid)
                EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, error.message)
            },
            ifRight = { newBalance ->
                EconomyResponse(amount, newBalance.toDouble(), EconomyResponse.ResponseType.SUCCESS, null)
            },
        )
    }
}
