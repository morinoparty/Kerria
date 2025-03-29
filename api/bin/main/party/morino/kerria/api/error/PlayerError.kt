package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * プレイヤー関連のエラー
 */
sealed class PlayerError(message: String) : KerriaError(message) {
    /**
     * プレイヤーが見つからない場合のエラー
     * @property uuid 見つからなかったプレイヤーのUUID
     */
    class PlayerNotFound(val uuid: String) : PlayerError("Player not found: $uuid")

    /**
     * プレイヤーが既に存在する場合のエラー
     * @property uuid 既に存在するプレイヤーのUUID
     */
    class PlayerAlreadyExists(val uuid: String) : PlayerError("Player already exists: $uuid")

    /**
     * プレイヤーがオフラインの場合のエラー
     * @property uuid オフラインのプレイヤーのUUID
     */
    class PlayerOffline(val uuid: String) : PlayerError("Player is offline: $uuid")
} 