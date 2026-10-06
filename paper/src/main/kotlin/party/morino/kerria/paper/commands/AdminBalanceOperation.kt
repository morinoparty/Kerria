package party.morino.kerria.paper.commands

import arrow.core.Either
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.error.KerriaError
import java.math.BigDecimal
import java.util.UUID

/**
 * 管理者用残高操作（set / give / take）の種類
 *
 * 操作ごとの違い（経済操作・メッセージキー・既定のログメッセージ）だけをまとめた Strategy。
 * 対象の解決やメッセージ送信といった共通の流れは [AdminEconomyCommand] が担う。
 *
 * @property key メッセージキーの接頭辞に使う操作名（例: "admin.give.success"）
 * @property logLabel 既定の取引ログメッセージに使う操作名
 * @property allowsMiniMessage `--message` で MiniMessage のタグを許可するか（受取人へ通知する give のみ）
 */
enum class AdminBalanceOperation(val key: String, val logLabel: String, val allowsMiniMessage: Boolean) {

    /** 残高を指定額に設定する */
    SET("set", "Admin set", allowsMiniMessage = false) {
        override fun apply(
            economy: EconomyManager,
            accountId: UUID,
            currencyId: Int,
            amount: BigDecimal,
            message: String,
        ): Either<KerriaError, BigDecimal> =
            economy.setBalance(accountId, currencyId, amount, message = message, treatePluginName = PLUGIN_NAME)
    },

    /** 指定額を入金する */
    GIVE("give", "Admin give", allowsMiniMessage = true) {
        override fun apply(
            economy: EconomyManager,
            accountId: UUID,
            currencyId: Int,
            amount: BigDecimal,
            message: String,
        ): Either<KerriaError, BigDecimal> =
            economy.deposit(accountId, currencyId, amount, message = message, treatePluginName = PLUGIN_NAME)
    },

    /** 指定額を出金する */
    TAKE("take", "Admin take", allowsMiniMessage = false) {
        override fun apply(
            economy: EconomyManager,
            accountId: UUID,
            currencyId: Int,
            amount: BigDecimal,
            message: String,
        ): Either<KerriaError, BigDecimal> =
            economy.withdraw(accountId, currencyId, amount, message = message, treatePluginName = PLUGIN_NAME)
    },
    ;

    /**
     * 1つのアカウントに対して操作を実行する
     *
     * @param economy 経済操作のマネージャー
     * @param accountId 対象アカウントのUUID
     * @param currencyId 通貨のID
     * @param amount 操作する金額
     * @param message 取引ログに記録するメッセージ
     * @return 操作後の残高、もしくはエラー
     */
    abstract fun apply(
        economy: EconomyManager,
        accountId: UUID,
        currencyId: Int,
        amount: BigDecimal,
        message: String,
    ): Either<KerriaError, BigDecimal>

    private companion object {
        // 取引ログに記録する実行元プラグイン名
        const val PLUGIN_NAME = "Kerria"
    }
}
