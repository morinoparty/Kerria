package party.morino.kerria.paper.gametest

import party.morino.fukurou.FukurouConfig
import party.morino.fukurou.junit.GameServerExtension
import party.morino.fukurou.plugin.PluginSource
import party.morino.fukurou.server.GameServer
import party.morino.fukurou.server.Isolation
import party.morino.fukurou.server.ServerSpec
import party.morino.fukurou.server.ServerType
import party.morino.fukurou.server.paper.Paper
import party.morino.fukurou.server.paper.PaperChannel

/**
 * Kerria と Vault を入れた Paper サーバー。
 *
 * `@ExtendWith(KerriaServer::class)` を付けたテストクラスはすべてこの 1 台のサーバーを共有する。
 * プレイヤーは OP の Alice と一般プレイヤーの Bob の 2 人。
 */
class KerriaServer : GameServerExtension() {
    // 送金する側。管理者コマンドの確認にも使えるよう OP にする
    val alice by player("Alice", op = true)

    // 受け取る側。権限による差が出ないよう一般プレイヤーのままにする
    val bob by player("Bob")

    /**
     * 起動するサーバーの種類。
     *
     * CI は FUKUROU_MINECRAFT_VERSION / FUKUROU_PAPER_CHANNEL（または -Pfukurou.*）を渡す。
     * 省略時は Kerria の paper-api と同じ 1.21.11 を、beta ビルドまで含めて使う。
     */
    override fun type(config: FukurouConfig): ServerType =
        Paper.fromProperties(config, defaultVersion = DEFAULT_VERSION, defaultChannel = PaperChannel.Beta)

    override fun ServerSpec.configure() {
        // result の id は paper-<version>-kerria になる
        label = "kerria"
        plugins {
            // gameTest タスクが shadowJar の成果物のパスを fukurou.plugin.kerria に渡す
            underTest(PluginSource.systemProperty("kerria"))
            // Kerria は起動時に Vault の Economy を登録するため、Vault 本体も入れる（runServer と同じ jar）
            dependency(PluginSource.url(VAULT_URL))
        }
        // ブロックは使わないので、リセットはプレイヤーの状態だけにする
        isolation = Isolation.Reset(arena = null)
    }

    /**
     * 各テストの前に、2 人の残高を既知の値にそろえる。
     *
     * Kerria の残高は fukurou のリセット対象外なので、テストの順序に依存しないようここで設定する。
     * `set` は実行者（コンソール）にしか結果を返さないため、プレイヤーのチャットを汚さない。
     */
    override suspend fun GameServer.setUp() {
        command("kerria set ${alice.name} $INITIAL_BALANCE")
        command("kerria set ${bob.name} $INITIAL_BALANCE")
    }

    companion object {
        /** -Pfukurou.minecraftVersion を省略したときの Minecraft のバージョン。 */
        const val DEFAULT_VERSION = "1.21.11"

        /** Vault 1.7.3 の配布 jar（paper/build.gradle.kts の runServer と同じもの）。 */
        const val VAULT_URL = "https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar"

        /** setUp で 2 人に設定する残高。 */
        const val INITIAL_BALANCE = 1000

        /** 取引メッセージの行（en_US: `Message: <text>` / ja_JP: `メッセージ: <text>`）に一致する正規表現を作る。 */
        fun transactionMessage(text: String): Regex = Regex("(?:Message|メッセージ): ${Regex.escape(text)}")

        /** コマンドの構文エラーや Kerria の失敗メッセージ。成功するはずの操作で出ていないことを確かめる。 */
        val COMMAND_FAILURES =
            Regex("(Unknown or incomplete command|Incorrect argument|failed|not found|失敗しました|見つかりません)")

        /** コマンド処理中の例外（cloud の "Exception executing command handler" など）のサーバーログ。 */
        val COMMAND_ERRORS = Regex("(Exception executing command handler|Unhandled exception|Exception while executing)")
    }
}
