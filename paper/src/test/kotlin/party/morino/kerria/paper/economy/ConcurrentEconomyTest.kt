package party.morino.kerria.paper.economy

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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@ExtendWith(KerriaTest::class)
class ConcurrentEconomyTest : KoinTest {

    private val economyManager: EconomyManager by inject()
    private val accountManager: AccountManager by inject()

    @Test
    @DisplayName("Concurrent balance updates all succeed")
    fun concurrentUpdatesSucceed() {
        // 非同期に実行されるコマンド（set と give の同時実行など）を想定し、複数スレッドから同時に更新する
        val accounts = (1..2).map { i ->
            accountManager.getOrCreateAccount(UUID.randomUUID(), "Concurrent$i").getOrNull()!!.accountId
        }
        val executor = Executors.newFixedThreadPool(8)
        val results = (1..40).map { i ->
            executor.submit<Boolean> {
                val account = accounts[i % accounts.size]
                if (i % 2 == 0) {
                    economyManager.setBalance(account, 1, BigDecimal("1000")).isRight()
                } else {
                    economyManager.deposit(account, 1, BigDecimal("10")).isRight()
                }
            }
        }
        executor.shutdown()
        executor.awaitTermination(60, TimeUnit.SECONDS)
        assertEquals(40, results.count { it.get() })
    }
}
