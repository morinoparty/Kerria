package party.morino.kerria.paper.commands.target

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.Account
import party.morino.kerria.paper.Kerria

/**
 * コマンドの対象プレイヤーをアカウントとして解決するヘルパー
 *
 * Bukkit の `getOfflinePlayerIfCached` はプロファイルキャッシュに無いオフラインプレイヤーに対して
 * null を返すため、キャッシュされていないプレイヤーをコマンドの対象にできない問題があった。
 * このヘルパーは Kerria が保持するアカウント名から解決することで、その問題を回避する。
 */
object TargetAccountResolver : KoinComponent {

    // セレクタをメインスレッドで解決するためのプラグイン本体
    private val plugin: Kerria by inject()

    /**
     * プレイヤー名から対象アカウントを解決する
     *
     * オンラインプレイヤーは最新の UUID から、オフラインプレイヤーは Kerria のアカウント名から
     * 解決する。これにより、サーバーのプロファイルキャッシュに無いオフラインプレイヤーでも、
     * Kerria のアカウントを持っていれば対象にできる。
     *
     * @param api Kerria の公開API
     * @param name 対象プレイヤー名
     * @return 解決したアカウント。存在しない場合は null
     */
    fun resolve(api: KerriaAPI, name: String): Account? {
        // オンラインプレイヤーは最新の UUID で解決する
        val online = Bukkit.getPlayerExact(name)
        if (online != null) {
            return api.getAccountManager().getAccount(online.uniqueId).getOrNull()
        }
        // オフラインは Kerria が保持するアカウント名から解決する（キャッシュ非依存）
        return api.getAccountManager().getAccountByPlayerName(name).getOrNull()
    }

    /**
     * プレイヤー名またはセレクタから対象アカウントの一覧を解決する
     *
     * プレイヤー名の場合は [resolve] と同じく最大1件を返す。セレクタの場合は該当する
     * オンラインプレイヤーのうち、Kerria のアカウントを持つプレイヤーをすべて返す。
     *
     * @param api Kerria の公開API
     * @param sender セレクタの基準となるコマンド送信者（`@p` や `@s` の解決に使う）
     * @param target 対象の指定
     * @return 解決したアカウントの一覧（該当なしの場合は空）
     * @throws IllegalArgumentException セレクタの構文が不正な場合
     */
    fun resolveAll(api: KerriaAPI, sender: CommandSender, target: PlayerTarget): List<Account> {
        if (!target.isSelector) {
            return listOfNotNull(resolve(api, target.input))
        }
        // セレクタで選ばれたプレイヤーを、アカウントを持つものに絞り込む
        return selectPlayers(sender, target.input)
            .mapNotNull { player -> api.getAccountManager().getAccount(player.uniqueId).getOrNull() }
            .distinctBy { it.accountId }
    }

    /**
     * セレクタをメインスレッドで解決してプレイヤーの一覧を返す
     *
     * コマンドは非同期に実行されるため、エンティティへアクセスするセレクタの解決は
     * メインスレッドに委ねる（非同期スレッドからの呼び出しはブロックして結果を待つ）。
     */
    private fun selectPlayers(sender: CommandSender, selector: String): List<Player> {
        val select = { Bukkit.selectEntities(sender, selector).filterIsInstance<Player>() }
        if (Bukkit.isPrimaryThread()) {
            return select()
        }
        return try {
            Bukkit.getScheduler().callSyncMethod(plugin, select).get()
        } catch (e: java.util.concurrent.ExecutionException) {
            // セレクタの構文エラーなどは呼び出し元へそのまま伝える
            throw e.cause ?: e
        }
    }
}
