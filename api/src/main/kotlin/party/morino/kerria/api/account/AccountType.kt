package party.morino.kerria.api.account

/**
 * アカウントの種類を表す列挙型
 *
 * プレイヤーアカウントとサービスアカウントを区別する。
 */
enum class AccountType {
    /** Minecraftプレイヤーのアカウント */
    PLAYER,

    /** プラグインやシステムが使用するサービスアカウント */
    SERVICE,
}
