package party.morino.kerria.paper.log

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.paper.KerriaTest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class LogManagerImplTest : KoinTest {

    private val accountManager: AccountManager by inject()
    private val economyManager: EconomyManager by inject()
    private val logManager: LogManager by inject()

    @Test
    @DisplayName("Transaction log is recorded after deposit")
    fun transactionLogRecordedAfterDeposit() {
        val uuid = UUID.randomUUID()
        val account = accountManager.getOrCreateAccount(uuid, "LogTestPlayer").getOrNull()!!

        economyManager.deposit(account.accountId, 1, BigDecimal("100.00"), "test deposit", "TestPlugin")

        val logs = logManager.getTransactionHistory(account.accountId, 10, 0).getOrNull()!!
        assertTrue(logs.isNotEmpty())
        assertEquals("test deposit", logs.first().message)
        assertEquals("TestPlugin", logs.first().treatePluginName)
    }

    @Test
    @DisplayName("Transaction history returns empty for new account")
    fun emptyHistoryForNewAccount() {
        val uuid = UUID.randomUUID()
        val account = accountManager.getOrCreateAccount(uuid, "NoLogPlayer").getOrNull()!!

        val logs = logManager.getTransactionHistory(account.accountId, 10, 0).getOrNull()!!
        assertTrue(logs.isEmpty())
    }

    @Test
    @DisplayName("Transaction history tracks caller plugin name")
    fun transactionTracksCallerPlugin() {
        val uuid = UUID.randomUUID()
        val account = accountManager.getOrCreateAccount(uuid, "PluginTrackPlayer").getOrNull()!!

        economyManager.deposit(account.accountId, 1, BigDecimal("50.00"), null, "ShopPlugin")

        val logs = logManager.getTransactionHistory(account.accountId).getOrNull()!!
        assertEquals("ShopPlugin", logs.first().treatePluginName)
    }

    @Test
    @DisplayName("Old logs are counted and deleted while recent logs remain")
    fun deleteLogsOlderThanCutoff() {
        val account = accountManager.getOrCreateAccount(UUID.randomUUID(), "LogClearPlayer").getOrNull()!!
        val id = account.accountId
        // 基準日時より前のログ 2 件と、現在時刻のログ 1 件を記録する
        val old = LocalDateTime.of(2000, 1, 1, 0, 0)
        logManager.logTransaction(id, id, 1, BigDecimal.ONE, "old-1", null, old)
        logManager.logTransaction(id, id, 1, BigDecimal.ONE, "old-2", null, old)
        logManager.logTransaction(id, id, 1, BigDecimal.ONE, "recent", null, LocalDateTime.now())

        val cutoff = LocalDateTime.of(2001, 1, 1, 0, 0)
        assertEquals(2L, logManager.countLogsOlderThan(cutoff).getOrNull())
        assertEquals(2, logManager.deleteLogsOlderThan(cutoff).getOrNull())

        // 古いログだけが消え、新しいログは残る
        assertEquals(0L, logManager.countLogsOlderThan(cutoff).getOrNull())
        val remaining = logManager.getTransactionHistory(id).getOrNull()!!
        assertEquals(listOf("recent"), remaining.map { it.message })
    }
}
