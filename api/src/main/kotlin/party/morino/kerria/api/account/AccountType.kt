package party.morino.kerria.api.account

/**
 * アカウントの種類を表す列挙型
 *
 * アカウントの用途に応じて種類を区別する。
 */
enum class AccountType {
    /** Minecraftプレイヤーのアカウント */
    PLAYER,

    /** サーバー全体を代表するアカウント */
    SERVER,

    /** 外部プラグインが使用するアカウント */
    PLUGIN,

    /** システム内部で使用するアカウント（税金・手数料など） */
    SYSTEM,
}
