package party.morino.kerria.paper.account

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.error.KerriaError
import party.morino.kerria.paper.KerriaTest
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class AccountManagerImplTest : KoinTest {

    private val accountManager: AccountManager by inject()
    private val economyManager: EconomyManager by inject()

    @Test
    @DisplayName("Create and retrieve player account")
    fun createAndRetrieveAccount() {
        val uuid = UUID.randomUUID()
        val result = accountManager.getOrCreateAccount(uuid, "TestPlayer")
        assertTrue(result.isRight())
        val account = result.getOrNull()!!
        assertEquals("TestPlayer", account.name)
    }

    @Test
    @DisplayName("Get balance returns zero for new account")
    fun getBalanceReturnsZero() {
        val uuid = UUID.randomUUID()
        val account = accountManager.getOrCreateAccount(uuid, "ZeroBalancePlayer").getOrNull()!!
        val balance = accountManager.getBalance(account.accountId, 1)
        assertTrue(balance.isRight())
        assertEquals(BigDecimal.ZERO, balance.getOrNull())
    }

    @Test
    @DisplayName("Deposit increases balance correctly")
    fun depositIncreasesBalance() {
        val uuid = UUID.randomUUID()
        val account = accountManager.getOrCreateAccount(uuid, "DepositPlayer").getOrNull()!!
        economyManager.deposit(account.accountId, 1, BigDecimal("500.00"))
        val balance = accountManager.getBalance(account.accountId, 1).getOrNull()!!
        assertEquals(BigDecimal("500.0000"), balance)
    }

    @Test
    @DisplayName("Withdraw fails on insufficient balance")
    fun withdrawFailsOnInsufficientBalance() {
        val uuid = UUID.randomUUID()
        val account = accountManager.getOrCreateAccount(uuid, "PoorPlayer").getOrNull()!!
        val result = economyManager.withdraw(account.accountId, 1, BigDecimal("100.00"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InsufficientBalance)
    }

    @Test
    @DisplayName("Account not found returns error")
    fun accountNotFoundReturnsError() {
        val result = accountManager.getAccount(UUID.randomUUID())
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.AccountNotFound)
    }
}
