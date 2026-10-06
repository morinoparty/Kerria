package party.morino.kerria.paper.gametest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * `/kerria give` の `--message` を、実際のクライアントのチャットで確かめる。
 *
 * メソッド名がそのまま fukurou のテスト id になる。
 */
@Tag("economy")
@ExtendWith(KerriaServer::class)
class AdminGiveMessageTest {
    /**
     * コンソールからセレクター `@a` と MiniMessage 付きのメッセージで付与する。
     *
     * セレクターがオンラインの全員に展開されること、`--message` 以降の空白を含む文字列が 1 つのメッセージになること、
     * MiniMessage のタグが解釈されて生のタグが表示されないことをまとめて確かめる。
     */
    @Test
    @DisplayName("Console give to @a delivers a MiniMessage message to everyone")
    suspend fun `give-selector-with-minimessage`(env: KerriaServer) {
        // setUp で送ったチャットを除外するため、コマンドの直前に印を付ける
        val aliceMark = env.alice.mark()
        val bobMark = env.bob.mark()

        env.server.command("kerria give @a 1000 --message <gold>Event reward")

        // 2 人とも付与の通知と取引メッセージを受け取る
        for ((player, mark) in listOf(env.alice to aliceMark, env.bob to bobMark)) {
            player.awaitChat(KerriaServer.transactionMessage("Event reward"), after = mark)
            // タグは色として解釈され、文字列として表示されない
            player.assertNoChat(Regex("<gold>"), after = mark)
        }
    }
}
