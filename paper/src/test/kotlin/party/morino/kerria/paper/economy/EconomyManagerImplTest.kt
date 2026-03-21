package party.morino.kerria.paper.economy

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
class EconomyManagerImplTest : KoinTest {

    private val economyManager: EconomyManager by inject()
    private val accountManager: AccountManager by inject()

    private fun createAccount(name: String): UUID {
        val uuid = UUID.randomUUID()
        return accountManager.getOrCreateAccount(uuid, name).getOrNull()!!.accountId
    }

    @Test
    @DisplayName("Deposit with zero amount returns error")
    fun depositZeroAmountReturnsError() {
        val accountId = createAccount("ZeroDeposit")
        val result = economyManager.deposit(accountId, 1, BigDecimal.ZERO)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InvalidAmount)
    }

    @Test
    @DisplayName("Deposit with negative amount returns error")
    fun depositNegativeAmountReturnsError() {
        val accountId = createAccount("NegDeposit")
        val result = economyManager.deposit(accountId, 1, BigDecimal("-100"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InvalidAmount)
    }

    @Test
    @DisplayName("Deposit with nonexistent currency returns error")
    fun depositNonexistentCurrencyReturnsError() {
        val accountId = createAccount("BadCurrency")
        val result = economyManager.deposit(accountId, 9999, BigDecimal("100"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.CurrencyNotFound)
    }

    @Test
    @DisplayName("Deposit returns updated balance")
    fun depositReturnsUpdatedBalance() {
        val accountId = createAccount("DepositReturn")
        val result = economyManager.deposit(accountId, 1, BigDecimal("250"))
        assertTrue(result.isRight())
        assertEquals(BigDecimal("250.0000"), result.getOrNull())
    }

    @Test
    @DisplayName("Withdraw returns remaining balance")
    fun withdrawReturnsRemainingBalance() {
        val accountId = createAccount("WithdrawReturn")
        economyManager.deposit(accountId, 1, BigDecimal("1000"))
        val result = economyManager.withdraw(accountId, 1, BigDecimal("300"))
        assertTrue(result.isRight())
        assertEquals(BigDecimal("700.0000"), result.getOrNull())
    }

    @Test
    @DisplayName("Withdraw with zero amount returns error")
    fun withdrawZeroAmountReturnsError() {
        val accountId = createAccount("ZeroWithdraw")
        val result = economyManager.withdraw(accountId, 1, BigDecimal.ZERO)
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InvalidAmount)
    }

    @Test
    @DisplayName("SetBalance sets exact amount")
    fun setBalanceSetsExactAmount() {
        val accountId = createAccount("SetBalance")
        economyManager.deposit(accountId, 1, BigDecimal("500"))
        val result = economyManager.setBalance(accountId, 1, BigDecimal("200"))
        assertTrue(result.isRight())
        assertEquals(BigDecimal("200.0000"), result.getOrNull())
    }

    @Test
    @DisplayName("SetBalance with negative amount returns error")
    fun setBalanceNegativeReturnsError() {
        val accountId = createAccount("NegSetBalance")
        val result = economyManager.setBalance(accountId, 1, BigDecimal("-50"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InvalidAmount)
    }

    @Test
    @DisplayName("Transfer moves funds between accounts")
    fun transferMovesFunds() {
        val fromId = createAccount("Sender")
        val toId = createAccount("Receiver")
        economyManager.deposit(fromId, 1, BigDecimal("1000"))

        val result = economyManager.transfer(fromId, toId, 1, BigDecimal("400"))
        assertTrue(result.isRight())

        val fromBalance = accountManager.getBalance(fromId, 1).getOrNull()!!
        val toBalance = accountManager.getBalance(toId, 1).getOrNull()!!
        assertEquals(BigDecimal("600.0000"), fromBalance)
        assertEquals(BigDecimal("400.0000"), toBalance)
    }

    @Test
    @DisplayName("Transfer to self returns error")
    fun transferToSelfReturnsError() {
        val accountId = createAccount("SelfTransfer")
        economyManager.deposit(accountId, 1, BigDecimal("1000"))
        val result = economyManager.transfer(accountId, accountId, 1, BigDecimal("100"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.TransferToSelf)
    }

    @Test
    @DisplayName("Transfer with insufficient balance returns error")
    fun transferInsufficientBalanceReturnsError() {
        val fromId = createAccount("PoorSender")
        val toId = createAccount("RichReceiver")
        val result = economyManager.transfer(fromId, toId, 1, BigDecimal("999"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InsufficientBalance)
    }
}
