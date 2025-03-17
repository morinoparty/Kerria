package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * プレイヤー関連のエラー
 */
sealed class PlayerError : KerriaError() {
    /**
     * プレイヤーが見つからない場合のエラー
     * @property uuid 見つからなかったプレイヤーのUUID
     */
    data class PlayerNotFound(val uuid: String) : PlayerError()

    /**
     * プレイヤーが既に存在する場合のエラー
     * @property uuid 既に存在するプレイヤーのUUID
     */
    data class PlayerAlreadyExists(val uuid: String) : PlayerError()

    /**
     * プレイヤーがオフラインの場合のエラー
     * @property uuid オフラインのプレイヤーのUUID
     */
    data class PlayerOffline(val uuid: String) : PlayerError()
} 