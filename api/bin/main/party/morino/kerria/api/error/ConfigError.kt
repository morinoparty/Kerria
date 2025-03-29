package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * 設定関連のエラー
 */
sealed class ConfigError(message: String) : KerriaError(message) {
    /**
     * 設定ファイルの読み込みに失敗した場合のエラー
     * @property message エラーメッセージ
     */
    class LoadError(override val message: String) : ConfigError(message)

    /**
     * 設定ファイルの保存に失敗した場合のエラー
     * @property message エラーメッセージ
     */
    class SaveError(override val message: String) : ConfigError(message)

    /**
     * 設定値が無効な場合のエラー
     * @property key 無効な設定のキー
     * @property value 無効な設定値
     * @property reason 無効である理由
     */
    class InvalidValue(val key: String, val value: String, val reason: String) : ConfigError("Invalid value for key: $key, value: $value, reason: $reason")

    /**
     * 必要な設定が見つからない場合のエラー
     * @property key 見つからない設定キー
     */
    class MissingConfig(val key: String) : ConfigError("Missing config key: $key")
} 