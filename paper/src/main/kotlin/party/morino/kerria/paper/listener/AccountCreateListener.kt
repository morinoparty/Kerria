package party.morino.kerria.paper.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.paper.Kerria

/**
 * プレイヤーの参加時に Kerria のアカウントを作成するリスナー
 *
 * これまでアカウントは Vault 経由の操作でしか作成されず、Vault を使うプラグインが無い環境では
 * 参加したプレイヤーが /pay や /kerria give の対象にならなかった。参加時に作成しておくことで、
 * 一度でも参加したプレイヤーは常にコマンドの対象にできる。
 */
class AccountCreateListener : Listener, KoinComponent {

    private val accountManager: AccountManager by inject()
    private val plugin: Kerria by inject()

    /**
     * ログイン前（非同期スレッド）にアカウントを作成する
     *
     * DB へのアクセスを伴うため、メインスレッドをブロックしない AsyncPlayerPreLoginEvent で行う。
     * ログインが拒否された場合は作成しないよう、他プラグインの判定後（MONITOR）に処理する。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return
        }
        // 作成に失敗してもログイン自体は妨げず、警告のみ出す
        accountManager.getOrCreateAccount(event.uniqueId, event.name).onLeft { error ->
            plugin.logger.warning("Failed to create account for ${event.name}: ${error.message}")
        }
    }
}
