package party.morino.kerria.paper.account

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.account.BankManager
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(party.morino.kerria.paper.KerriaTest::class)
class BankManagerImplTest : KoinTest {

    private val bankManager: BankManager by inject()

    // テスト間の衝突を避けるため一意な銀行名を生成する
    private fun uniqueName(prefix: String): String = "$prefix-${UUID.randomUUID()}"

    @Test
    @DisplayName("Create, get and delete a bank")
    fun createGetDelete() {
        val name = uniqueName("guild")
        val owner = UUID.randomUUID()

        val created = bankManager.createBank(name, owner)
        assertTrue(created.isRight())
        assertEquals(owner, created.getOrNull()!!.ownerUuid)

        assertTrue(bankManager.getBank(name).isRight())

        assertTrue(bankManager.deleteBank(name).isRight())
        val afterDelete = bankManager.getBank(name)
        assertTrue(afterDelete.isLeft())
        assertTrue(afterDelete.leftOrNull() is KerriaError.BankNotFound)
    }

    @Test
    @DisplayName("Creating a bank with a duplicate name fails")
    fun duplicateNameFails() {
        val name = uniqueName("town")
        bankManager.createBank(name, UUID.randomUUID())

        val second = bankManager.createBank(name, UUID.randomUUID())
        assertTrue(second.isLeft())
        assertTrue(second.leftOrNull() is KerriaError.BankAlreadyExists)
    }

    @Test
    @DisplayName("Deposit and withdraw adjust the bank balance")
    fun depositWithdraw() {
        val name = uniqueName("vault")
        bankManager.createBank(name, UUID.randomUUID())

        bankManager.deposit(name, 1, BigDecimal("1000"))
        bankManager.withdraw(name, 1, BigDecimal("400"))

        val balance = bankManager.bankBalance(name, 1).getOrNull()!!
        assertEquals(0, balance.compareTo(BigDecimal("600")))
    }

    @Test
    @DisplayName("Withdraw beyond balance returns insufficient funds")
    fun withdrawInsufficient() {
        val name = uniqueName("poor")
        bankManager.createBank(name, UUID.randomUUID())

        val result = bankManager.withdraw(name, 1, BigDecimal("100"))
        assertTrue(result.isLeft())
        assertTrue(result.leftOrNull() is KerriaError.InsufficientBalance)
    }

    @Test
    @DisplayName("Owner and member checks reflect membership")
    fun ownerAndMember() {
        val name = uniqueName("club")
        val owner = UUID.randomUUID()
        val member = UUID.randomUUID()
        val stranger = UUID.randomUUID()
        bankManager.createBank(name, owner)

        assertTrue(bankManager.isOwner(name, owner).getOrNull()!!)
        assertTrue(bankManager.isMember(name, owner).getOrNull()!!)

        bankManager.addMember(name, member)
        assertTrue(bankManager.isMember(name, member).getOrNull()!!)
        assertFalse(bankManager.isOwner(name, member).getOrNull()!!)

        assertFalse(bankManager.isMember(name, stranger).getOrNull()!!)
    }

    @Test
    @DisplayName("Operations on an unknown bank return BankNotFound")
    fun unknownBank() {
        val missing = uniqueName("ghost")
        assertTrue(bankManager.bankBalance(missing, 1).leftOrNull() is KerriaError.BankNotFound)
        assertTrue(bankManager.deposit(missing, 1, BigDecimal("10")).leftOrNull() is KerriaError.BankNotFound)
        assertTrue(bankManager.isOwner(missing, UUID.randomUUID()).leftOrNull() is KerriaError.BankNotFound)
    }
}
