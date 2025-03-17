package party.morino.kerria.api.files

import arrow.core.Either
import party.morino.kerria.api.error.KerriaError

/**
 * 設定管理機能を提供するインターフェース
 *
 * プラグインの設定ファイルの読み込み、保存、リロードなどの
 * 設定管理機能を提供します。設定値はすべて[Config]クラスを通じて
 * アクセスされます。
 */
interface ConfigManager {

    /**
     * 現在の設定を取得します
     *
     * @return 現在適用されている[Config]インスタンス
     */
    fun getConfig(): Config

    /**
     * 設定ファイルを再読み込みします
     *
     * ディスク上の設定ファイルを読み込み、現在の設定を更新します。
     * この操作は既存の設定値をすべて上書きします。
     *
     * @return リロードの結果。成功時はUnit、失敗時は[KerriaError]を返します
     * @throws DatabaseError 設定ファイルの読み込みに失敗した場合
     */
    fun reloadConfig(): Either<KerriaError, Unit>
}