package party.morino.kerria.paper.economy

import net.milkbowl.vault.economy.EconomyResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.paper.KerriaTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class VaultEconomyTest : KoinTest {

    private val server: ServerMock by inject()
    private val accountManager: AccountManager by inject()

    private lateinit var economy: VaultEconomy
    private lateinit var player: PlayerMock

    @BeforeEach
    fun setup() {
        economy = VaultEconomy()
        player = server.addPlayer("VaultTestPlayer")
        // アカウントを作成
        accountManager.getOrCreateAccount(player.uniqueId, player.name)
    }

    @Test
    @DisplayName("getBalance returns 0 for new account")
    fun getBalanceReturnsZero() {
        val balance = economy.getBalance(player)
        assertEquals(0.0, balance)
    }

    @Test
    @DisplayName("depositPlayer increases balance")
    fun depositIncreasesBalance() {
        val response = economy.depositPlayer(player, 1000.0)
        assertEquals(EconomyResponse.ResponseType.SUCCESS, response.type)
        assertEquals(1000.0, economy.getBalance(player))
    }

    @Test
    @DisplayName("withdrawPlayer fails on insufficient funds")
    fun withdrawFailsOnInsufficientFunds() {
        val response = economy.withdrawPlayer(player, 100.0)
        assertEquals(EconomyResponse.ResponseType.FAILURE, response.type)
    }

    @Test
    @DisplayName("Bank support is enabled and bank operations work")
    fun bankOperationsWork() {
        assertTrue(economy.hasBankSupport())

        // プレイヤーを所有者に銀行を作成する
        val create = economy.createBank("VaultBank", player.name)
        assertEquals(EconomyResponse.ResponseType.SUCCESS, create.type)

        // 入金して残高を確認する
        val deposit = economy.bankDeposit("VaultBank", 500.0)
        assertEquals(EconomyResponse.ResponseType.SUCCESS, deposit.type)
        assertEquals(500.0, economy.bankBalance("VaultBank").balance)

        // 所有者判定と一覧を確認する
        assertEquals(EconomyResponse.ResponseType.SUCCESS, economy.isBankOwner("VaultBank", player.name).type)
        assertTrue(economy.getBanks().contains("VaultBank"))
    }

    @Test
    @DisplayName("Deposit then withdraw succeeds with sufficient funds")
    fun depositThenWithdrawSucceeds() {
        economy.depositPlayer(player, 500.0)
        val response = economy.withdrawPlayer(player, 200.0)
        assertEquals(EconomyResponse.ResponseType.SUCCESS, response.type)
        assertEquals(300.0, economy.getBalance(player))
    }
}
