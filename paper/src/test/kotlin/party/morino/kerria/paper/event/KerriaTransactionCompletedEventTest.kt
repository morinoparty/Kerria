package party.morino.kerria.paper.event

import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.paper.KerriaTest
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class KerriaTransactionCompletedEventTest : KoinTest {

    private val accountManager: AccountManager by inject()
    private val economyManager: EconomyManager by inject()

    /** 発火した確定イベントを収集するリスナー */
    private class Capture : Listener {
        val events = mutableListOf<KerriaTransactionCompletedEvent>()

        @EventHandler
        fun onCompleted(event: KerriaTransactionCompletedEvent) {
            events.add(event)
        }
    }

    private fun withListener(block: (Capture) -> Unit) {
        val capture = Capture()
        KerriaTest.server.pluginManager.registerEvents(capture, KerriaTest.plugin)
        try {
            block(capture)
        } finally {
            HandlerList.unregisterAll(capture)
        }
    }

    @Test
    @DisplayName("Successful deposit fires completed event with resulting balance")
    fun depositFiresCompleted() {
        val account = accountManager.getOrCreateAccount(UUID.randomUUID(), "PostDeposit").getOrNull()!!
        withListener { capture ->
            economyManager.deposit(account.accountId, 1, BigDecimal("500"))

            val event = capture.events.single()
            assertEquals(KerriaTransactionEvent.TransactionType.DEPOSIT, event.type)
            assertEquals(0, event.toBalance.compareTo(BigDecimal("500")))
        }
    }

    @Test
    @DisplayName("Failed withdraw does not fire completed event")
    fun failedWithdrawDoesNotFire() {
        val account = accountManager.getOrCreateAccount(UUID.randomUUID(), "PostPoor").getOrNull()!!
        withListener { capture ->
            // 残高不足で出金は失敗し、確定イベントは発火しない
            val result = economyManager.withdraw(account.accountId, 1, BigDecimal("999"))
            assertTrue(result.isLeft())
            assertTrue(capture.events.isEmpty())
        }
    }

    @Test
    @DisplayName("Transfer fires completed event with both balances")
    fun transferFiresWithBothBalances() {
        val from = accountManager.getOrCreateAccount(UUID.randomUUID(), "PostFrom").getOrNull()!!
        val to = accountManager.getOrCreateAccount(UUID.randomUUID(), "PostTo").getOrNull()!!
        economyManager.deposit(from.accountId, 1, BigDecimal("1000"))

        withListener { capture ->
            economyManager.transfer(from.accountId, to.accountId, 1, BigDecimal("300"))

            val event = capture.events.single { it.type == KerriaTransactionEvent.TransactionType.TRANSFER }
            assertEquals(0, event.fromBalance.compareTo(BigDecimal("700")))
            assertEquals(0, event.toBalance.compareTo(BigDecimal("300")))
        }
    }
}
