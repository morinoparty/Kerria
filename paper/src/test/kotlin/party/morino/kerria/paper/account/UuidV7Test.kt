package party.morino.kerria.paper.account

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.common.database.UuidV7
import party.morino.kerria.paper.KerriaTest
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Kerria が採番するアカウントID（UUID v7）のテスト
 */
@ExtendWith(KerriaTest::class)
class UuidV7Test : KoinTest {

    private val accountManager: AccountManager by inject()

    @Test
    @DisplayName("Generated UUID has version 7, RFC variant and embeds the timestamp")
    fun generatesVersion7() {
        val uuid = UuidV7.generate(epochMillis = 1_700_000_000_000L)
        assertEquals(7, uuid.version())
        assertEquals(2, uuid.variant())
        // 上位 48 ビットに時刻が入っている
        assertEquals(1_700_000_000_000L, uuid.mostSignificantBits ushr 16)
    }

    @Test
    @DisplayName("New player accounts get a UUID v7 account id")
    fun accountIdIsVersion7() {
        val account = accountManager.getOrCreateAccount(UUID.randomUUID(), "UuidV7Player").getOrNull()!!
        assertEquals(7, account.accountId.version())
        // プレイヤーUUID（Minecraft の v4）とは別の値になる
        assertTrue(account.accountId != account.playerUniqueId)
    }
}
