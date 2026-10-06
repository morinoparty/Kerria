package party.morino.kerria.paper.commands

import com.mojang.brigadier.CommandDispatcher
import io.leangen.geantyref.TypeToken
import org.incendo.cloud.CommandManager
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.setting.ManagerSetting
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.brigadier.CloudBrigadierManager
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.internal.CommandRegistrationHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import party.morino.kerria.paper.commands.target.PlayerTarget
import party.morino.kerria.paper.commands.target.PlayerTargetParser
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * `/pay` や `/kerria give` と同じ形のコマンド構文が、Cloud と Brigadier の双方で
 * 解析できることを確認するテスト
 *
 * 実際のコマンドクラスは Koin や Bukkit に依存するため、同じ引数構成の代替コマンドで検証する。
 */
class CommandSyntaxTest {

    /** テスト用のコマンド送信者 */
    class TestSender

    /** 解析結果を記録する代替コマンド（テストはパラメータ名を保持しないため @Argument で名前を指定する） */
    class RecordingCommands {
        val calls = mutableListOf<List<Any?>>()

        @Command("pay <player> <amount> [currencyId]")
        fun pay(
            sender: TestSender,
            @Argument("player") player: String,
            @Argument("amount") amount: Double,
            @Argument("currencyId") @Default("1") currencyId: Int,
            @Flag("message") @Greedy message: String?,
        ) {
            calls += listOf(player, amount, currencyId, message)
        }

        @Command("give <player> <amount> [currencyId]")
        fun give(
            sender: TestSender,
            @Argument("player") player: PlayerTarget,
            @Argument("amount") amount: Double,
            @Argument("currencyId") @Default("1") currencyId: Int,
            @Flag("message") @Greedy message: String?,
        ) {
            calls += listOf(player.input, amount, currencyId, message)
        }
    }

    private lateinit var manager: CommandManager<TestSender>
    private lateinit var commands: RecordingCommands

    @BeforeEach
    fun setUp() {
        manager = object : CommandManager<TestSender>(
            ExecutionCoordinator.simpleCoordinator(),
            CommandRegistrationHandler.nullCommandRegistrationHandler(),
        ) {
            override fun hasPermission(sender: TestSender, permission: String): Boolean = true
        }
        // 本番（Kerria.createCommandManager）と同じく、省略可能な引数の手前でもフラグを解析できるようにする
        manager.settings().set(ManagerSetting.LIBERAL_FLAG_PARSING, true)
        manager.parserRegistry().registerParserSupplier(TypeToken.get(PlayerTarget::class.java)) {
            PlayerTargetParser()
        }
        commands = RecordingCommands()
        AnnotationParser(manager, TestSender::class.java).parse(commands)
    }

    /** Cloud でコマンドを実行し、記録された引数を返す */
    private fun execute(input: String): List<Any?> {
        manager.commandExecutor().executeCommand(TestSender(), input).join()
        return commands.calls.last()
    }

    /** Cloud のコマンドツリーから Brigadier のノードを生成し、入力を解析できるか判定する */
    private fun brigadierAccepts(label: String, input: String): Boolean {
        val brigadier = CloudBrigadierManager(manager, SenderMapper.identity())
        val node = brigadier.literalBrigadierNodeFactory().createNode(
            label,
            manager.commandTree().getNamedNode(label)!!,
            { 1 },
        ) { _, _ -> true }
        val dispatcher = CommandDispatcher<TestSender>()
        dispatcher.root.addChild(node)
        val result = dispatcher.parse(input, TestSender())
        return result.exceptions.isEmpty() && result.context.command != null && !result.reader.canRead()
    }

    @Test
    @DisplayName("Message flag captures the rest of the input")
    fun messageFlagIsGreedy() {
        assertEquals(listOf("Bob", 100.0, 1, "glass fee"), execute("pay Bob 100 --message glass fee"))
        assertEquals(listOf("Bob", 5.0, 2, "hi"), execute("pay Bob 5 2 --message hi"))
        assertEquals(listOf("Bob", 10.0, 1, null), execute("pay Bob 10"))
    }

    @Test
    @DisplayName("Selector target is parsed with the message flag")
    fun selectorTargetIsParsed() {
        assertEquals(
            listOf("@a[distance=..10]", 100.0, 1, "<gold>Event reward"),
            execute("give @a[distance=..10] 100 --message <gold>Event reward"),
        )
    }

    @Test
    @DisplayName("Brigadier accepts the message flag with and without currency id")
    fun brigadierAcceptsFlags() {
        assertNotNull(manager.commandTree().getNamedNode("pay"))
        assertTrue(brigadierAccepts("pay", "pay Bob 100"))
        assertTrue(brigadierAccepts("pay", "pay Bob 100 2"))
        assertTrue(brigadierAccepts("pay", "pay Bob 100 --message glass fee"))
        assertTrue(brigadierAccepts("pay", "pay Bob 100 2 --message glass fee"))
    }
}
