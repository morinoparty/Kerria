package party.morino.kerria.paper.gametest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import party.morino.fukurou.FukurouConfig
import party.morino.fukurou.junit.GameServerExtension
import party.morino.fukurou.plugin.PluginSource
import party.morino.fukurou.server.CommandCheck
import party.morino.fukurou.server.GameServer
import party.morino.fukurou.server.Isolation
import party.morino.fukurou.server.ServerSpec
import party.morino.fukurou.server.ServerType
import party.morino.fukurou.server.paper.Paper
import party.morino.fukurou.server.paper.PaperChannel

class TmpSmokeServer : GameServerExtension() {
    override fun type(config: FukurouConfig): ServerType =
        Paper.fromProperties(config, defaultVersion = "1.21.11", defaultChannel = PaperChannel.Alpha)
    override fun ServerSpec.configure() {
        label = if (System.getProperty("tmp.vault") == "true") "tmp-smoke-vault" else "tmp-smoke-novault"
        isolation = Isolation.Reset(arena = null)
        plugins {
            underTest(PluginSource.systemProperty("kerria"))
            if (System.getProperty("tmp.vault") == "true") dependency(PluginSource.url(KerriaServer.VAULT_URL))
        }
    }
}

@ExtendWith(TmpSmokeServer::class)
class TmpSmokeTest {
    @Test
    suspend fun smoke(server: GameServer) {
        val cmds = listOf(
            "kerria reload", "kerria currency list", "kerria log player X",
            "kerria give @a 1000 --message <gold>x", "kerria give Nobody 10 1 --message hi there",
            "kerria take Nobody 10", "kerria set Nobody 10", "pay Bob 1", "kerria pay Bob 1",
            "minecraft:help kerria", "minecraft:help pay",
        )
        for (c in cmds) {
            val r = server.command(c, CommandCheck.ReturnRaw)
            println("CMD[$c] => ${r.text}")
            server.awaitTicks(20)
        }
    }
}
