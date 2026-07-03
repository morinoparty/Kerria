package party.morino.kerria.paper.files

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.files.MessageManager
import kotlin.test.assertTrue

/**
 * /kerria reload が依存する再読み込みロジックのテスト
 *
 * コマンド本体は ConfigManager と MessageManager の再読み込みを合成するだけなので、
 * それぞれが成功する（Either.Right）ことを確認する。
 */
@ExtendWith(party.morino.kerria.paper.KerriaTest::class)
class ReloadTest : KoinTest {

    private val configManager: ConfigManager by inject()
    private val messageManager: MessageManager by inject()

    @Test
    @DisplayName("Reload config returns success")
    fun reloadConfigReturnsSuccess() {
        assertTrue(configManager.reloadConfig().isRight())
    }

    @Test
    @DisplayName("Reload messages returns success")
    fun reloadMessagesReturnsSuccess() {
        assertTrue(messageManager.reloadMessages().isRight())
    }
}
