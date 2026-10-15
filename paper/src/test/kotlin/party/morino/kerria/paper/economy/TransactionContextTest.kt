package party.morino.kerria.paper.economy

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockbukkit.mockbukkit.ServerMock
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.economy.TransactionContext
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.paper.KerriaTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * TransactionContext に設定したメッセージが Vault 経由の取引ログに付くことのテスト
 */
@ExtendWith(KerriaTest::class)
class TransactionContextTest : KoinTest {

    private val server: ServerMock by inject()
    private val accountManager: AccountManager by inject()
    private val logManager: LogManager by inject()

    @Test
    @DisplayName("Vault transactions inside withMessage are logged with the message as plain text")
    fun vaultTransactionUsesContextMessage() {
        val player = server.addPlayer("ContextPlayer")
        val account = accountManager.getOrCreateAccount(player.uniqueId, player.name).getOrNull()!!
        val economy = VaultEconomy()

        // タグはエスケープされ、プレーンテキストとして保存される
        TransactionContext.withMessage("<red>ダイヤモンド ×16 を購入") {
            economy.depositPlayer(player, 100.0)
        }
        // ブロックの外では解除されている
        assertNull(TransactionContext.currentMessage())
        economy.depositPlayer(player, 1.0)

        val logs = logManager.getTransactionHistory(account.accountId, limit = 2).getOrNull()!!
        val messages = logs.sortedBy { it.id }.map { it.message }
        assertEquals(listOf("\\<red>ダイヤモンド ×16 を購入", null), messages)
    }

    @Test
    @DisplayName("Nested withMessage restores the outer message")
    fun nestedWithMessageRestoresOuter() {
        TransactionContext.withMessage("outer") {
            TransactionContext.withMessage("inner") {
                assertEquals("inner", TransactionContext.currentMessage())
            }
            assertEquals("outer", TransactionContext.currentMessage())
        }
        assertNull(TransactionContext.currentMessage())
    }
}
