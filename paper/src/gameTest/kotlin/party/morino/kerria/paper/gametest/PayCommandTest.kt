package party.morino.kerria.paper.gametest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import party.morino.fukurou.log.LogMark
import party.morino.fukurou.pause
import party.morino.fukurou.screenshot
import kotlin.time.Duration.Companion.seconds

/**
 * `/pay` と `/kerria pay` の引数の解釈を、実際のクライアントから送ったコマンドで確かめる。
 *
 * どのテストも setUp で 2 人の残高を [KerriaServer.INITIAL_BALANCE] にそろえてから始まる。
 * メソッド名がそのまま fukurou のテスト id になる。
 */
@Tag("economy")
@ExtendWith(KerriaServer::class)
class PayCommandTest {
    /**
     * 空白を含む `--message` が 1 つのメッセージとして受け取り手に届くことを確かめる。
     */
    @Test
    @DisplayName("Pay with a message shows the message to the receiver")
    suspend fun `pay-with-message`(env: KerriaServer) {
        val aliceMark = env.alice.mark()
        val bobMark = env.bob.mark()

        env.alice.sendCommand("pay Bob 100 --message glass fee")

        // 受け取り手にも送り手にも、空白で切れていないメッセージが表示される
        env.bob.awaitChat(KerriaServer.transactionMessage("glass fee"), after = bobMark)
        env.alice.awaitChat(KerriaServer.transactionMessage("glass fee"), after = aliceMark)
        env.alice.assertNoChat(KerriaServer.COMMAND_FAILURES, after = aliceMark)

        // 送金後の残高を /balance（Alice）と /bal（Bob）で確認する（setUp で 2 人とも 1000）
        env.alice.sendCommand("balance")
        env.bob.sendCommand("bal")
        env.alice.awaitChat(balanceOf("900"), after = aliceMark)
        env.bob.awaitChat(balanceOf("1,?100"), after = bobMark)
        // チャットが消える前に、2 人の画面を残す
        screenshot(env.alice, env.bob, name = "balance-after-pay")
    }

    /**
     * フラグも通貨 ID も省略した `/kerria pay` が、既定の通貨で成功することを確かめる。
     */
    @Test
    @DisplayName("Kerria pay without a flag uses the default currency")
    suspend fun `kerria-pay-without-flag`(env: KerriaServer) {
        val aliceMark = env.alice.mark()
        val serverMark = env.server.mark()

        env.alice.sendCommand("kerria pay Bob 10")

        assertPaySucceeded(env, aliceMark, serverMark)
    }

    /**
     * 通貨 ID と `--message` を両方指定しても、通貨 ID がメッセージに吸い込まれずに解釈されることを確かめる。
     */
    @Test
    @DisplayName("Pay with an explicit currency id and a message succeeds")
    suspend fun `pay-with-currency-and-message`(env: KerriaServer) {
        val aliceMark = env.alice.mark()
        val bobMark = env.bob.mark()
        val serverMark = env.server.mark()

        env.alice.sendCommand("pay Bob 5 1 --message hi")

        assertPaySucceeded(env, aliceMark, serverMark)
        env.bob.awaitChat(KerriaServer.transactionMessage("hi"), after = bobMark)
    }

    /**
     * Alice に送金成功の通知が届き、失敗や例外が出ていないことを確かめる。
     *
     * @param env テスト対象のサーバー
     * @param aliceMark コマンド送信前の Alice のクライアントログの位置
     * @param serverMark コマンド送信前のサーバーログの位置
     */
    private suspend fun assertPaySucceeded(
        env: KerriaServer,
        aliceMark: LogMark,
        serverMark: LogMark,
    ) {
        // en_US: "Sent <amount> to Bob." / ja_JP: "Bob に <amount> を送金しました。"
        env.alice.awaitChat(SENT_TO_BOB, after = aliceMark)
        // 成功の通知の後に失敗の行が続いていないか、少し待ってから確かめる
        pause(1.seconds)
        env.alice.assertNoChat(KerriaServer.COMMAND_FAILURES, after = aliceMark)
        env.server.assertNoLog(KerriaServer.COMMAND_ERRORS, after = serverMark)
    }

    private companion object {
        /** Alice が Bob への送金に成功したときのチャット。 */
        val SENT_TO_BOB = Regex("(?:Sent .+ to Bob|Bob に .+ を送金しました)")

        /**
         * 残高表示（en_US: `Your balance is <amount>.` / ja_JP: `あなたの残高は<amount>です。`）に一致する正規表現を作る。
         *
         * @param amount 金額の正規表現（桁区切りや小数部の有無に依存しないよう、整数部のみを指定する）
         */
        fun balanceOf(amount: String): Regex = Regex("(?:Your balance is|あなたの残高は)\\D*\\b$amount\\b")
    }
}
