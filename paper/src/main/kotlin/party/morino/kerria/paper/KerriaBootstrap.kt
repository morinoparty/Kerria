package party.morino.kerria.paper

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

/**
 * Kerria の Bootstrap
 *
 * コマンドマネージャーは Bootstrap で作成すると onEnable 時点で登録を受け付けなくなるため、
 * [Kerria] の onEnable で作成する。ここではプラグインのインスタンス生成のみを行う。
 */
@Suppress("unused", "UnstableApiUsage")
class KerriaBootstrap : PluginBootstrap {

    companion object {
        // Bootstrap が実行されたか（実際の Paper サーバー上かどうか）
        // テスト環境(MockBukkit)では Bootstrap が実行されず、コマンドマネージャーも作成できないため判定に使う
        var bootstrapped: Boolean = false
            private set
    }

    override fun bootstrap(context: BootstrapContext) {
        bootstrapped = true
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Kerria()
    }
}
