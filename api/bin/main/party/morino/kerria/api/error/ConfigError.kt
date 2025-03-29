package party.morino.kerria.api.error

import party.morino.kerria.api.error.KerriaError

/**
 * 設定関連のエラー
 */
sealed class ConfigError : KerriaError() {
    /**
     * 設定ファイルの読み込みに失敗した場合のエラー
     * @property message エラーメッセージ
     */
    data class LoadError(val message: String) : ConfigError()

    /**
     * 設定ファイルの保存に失敗した場合のエラー
     * @property message エラーメッセージ
     */
    data class SaveError(val message: String) : ConfigError()

    /**
     * 設定値が無効な場合のエラー
     * @property key 無効な設定のキー
     * @property value 無効な設定値
     * @property reason 無効である理由
     */
    data class InvalidValue(val key: String, val value: String, val reason: String) : ConfigError()

    /**
     * 必要な設定が見つからない場合のエラー
     * @property key 見つからない設定キー
     */
    data class MissingConfig(val key: String) : ConfigError()
} 