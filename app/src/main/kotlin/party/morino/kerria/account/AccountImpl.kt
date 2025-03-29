package party.morino.kerria.account

import org.bukkit.OfflinePlayer
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.Account
import party.morino.kerria.api.currency.Currency
import party.morino.kerria.api.log.Log
import party.morino.kerria.model.database.AccountTable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * アカウントエンティティの実装クラス
 *
 * このクラスは[Account]インターフェースを実装し、データベース上のアカウント情報を表現します。
 * [UUIDEntity]を継承し、Exposedフレームワークを使用してデータベースとのマッピングを行います。
 * アカウントIDとしてUUIDを主キーとして使用します。
 *
 * @property _playerUniqueId プレイヤーのUUID（文字列形式）
 * @property _playerName プレイヤーの名前
 * @property _balance アカウントの残高（BigDecimal型）
 */
class AccountImpl(id: EntityID<UUID>) : UUIDEntity(id), Account, KoinComponent {

    val api : KerriaAPI by inject()

    companion object : UUIDEntityClass<AccountImpl>(AccountTable) {
        /**
         * オフラインプレイヤーからアカウントを検索します
         *
         * @param offlinePlayer 検索対象のオフラインプレイヤー
         * @return 見つかったアカウント、または null
         */
        fun findByOfflinePlayer(offlinePlayer: OfflinePlayer) = 
            find { AccountTable.playerUniqueId eq offlinePlayer.uniqueId.toString() }.firstOrNull()

        /**
         * アカウントUUIDからアカウントを検索します
         *
         * @param accountUniqueId 検索対象のアカウントUUID
         * @return 見つかったアカウント、または null
         */
        fun findByAccountUniqueId(accountUniqueId: UUID) = findById(accountUniqueId)
    }

    private var _playerUniqueId by AccountTable.playerUniqueId
    private var _playerName by AccountTable.playerName
    private var _balance by AccountTable.balance

    /**
     * アカウントのUUIDを取得します
     *
     * @return アカウントのUUID
     */
    override fun getAccountUniqueId(): UUID = id.value

    /**
     * プレイヤーのUUIDを取得します
     *
     * @return プレイヤーのUUID
     */
    override fun getPlayerUniqueId(): UUID = UUID.fromString(_playerUniqueId)

    /**
     * プレイヤーの名前を取得します
     *
     * @return プレイヤーの名前
     */
    override fun getPlayerName(): String = _playerName

    /**
     * アカウントの残高を取得します
     *
     * @return アカウントの残高
     */
    override suspend fun getBalance(): BigDecimal {
        val defaultCurrency = api.getCurrencyManager().getDefaultCurrency().getOrNull() ?: return BigDecimal.ZERO
        return getBalance(defaultCurrency)
    }


    override suspend fun getBalance(currency: Currency): BigDecimal {
        TODO("Not yet implemented")
    }

    /**
     * アカウントの残高を設定します
     *
     * @param balance 設定する残高
     */
    override suspend fun setBalance(balance: BigDecimal) {
        val defaultCurrency = api.getCurrencyManager().getDefaultCurrency().getOrNull() ?: return
        setBalance(balance, defaultCurrency)
    }

    override suspend fun setBalance(balance: BigDecimal, currency: Currency) {
        TODO("Not yet implemented")
    }

    /**
     * アカウントの取引履歴を取得します
     *
     * @param since この日時以降の履歴を取得
     * @param until この日時以前の履歴を取得
     * @param limit 取得する履歴の最大件数
     * @param offset 取得開始位置
     * @return 取引履歴のリスト
     */
    override suspend fun getLogs(since: LocalDateTime, until: LocalDateTime, limit: Int, offset: Int): List<Log> {
        // TODO: LogManagerを使用してログを取得する実装を追加
        return emptyList()
    }
}