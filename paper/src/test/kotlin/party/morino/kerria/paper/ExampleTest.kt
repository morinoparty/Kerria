package party.morino.kerria.paper

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockbukkit.mockbukkit.ServerMock
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(KerriaTest::class)
class ExampleTest : KoinTest {

    private val server: ServerMock by inject()

    @Test
    @DisplayName("Plugin is enabled successfully")
    fun pluginIsEnabled() {
        val plugin = KerriaTest.plugin
        assertNotNull(plugin)
        assertTrue(plugin.isEnabled)
    }

    @Test
    @DisplayName("Server has the plugin loaded")
    fun serverHasPlugin() {
        val pluginManager = server.pluginManager
        val plugin = pluginManager.getPlugin("Kerria")
        assertNotNull(plugin)
    }
}
