package party.morino.kerria.paper

import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager

@Suppress("unused", "UnstableApiUsage")
class KerriaBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        val commandManager: PaperCommandManager<CommandSourceStack> =
            PaperCommandManager
                .builder()
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildBootstrapped(context)

        // TODO: コマンドの登録をここで行う
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Kerria()
    }
}
