package party.morino.kerria.paper.commands

import org.bukkit.Bukkit
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.Account

/**
 * コマンドの対象プレイヤーをアカウントとして解決するヘルパー
 *
 * Bukkit の `getOfflinePlayerIfCached` はプロファイルキャッシュに無いオフラインプレイヤーに対して
 * null を返すため、キャッシュされていないプレイヤーをコマンドの対象にできない問題があった。
 * このヘルパーは Kerria が保持するアカウント名から解決することで、その問題を回避する。
 */
object TargetAccountResolver {

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
}
