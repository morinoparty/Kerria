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

    companion object {
        // コマンドマネージャーをBootstrapで作成し、プラグインのonEnable時にコマンド登録で使用する
        // テスト環境(MockBukkit)ではBootstrapが実行されないためnullableとする
        var commandManager: PaperCommandManager<CommandSourceStack>? = null
    }

    override fun bootstrap(context: BootstrapContext) {
        commandManager = PaperCommandManager
            .builder()
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildBootstrapped(context)
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Kerria()
    }
}
