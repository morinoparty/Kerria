package party.morino.kerria.paper.commands.target

/**
 * コマンドの対象プレイヤーの指定（プレイヤー名、またはエンティティセレクタ）
 *
 * 入力の解析時点ではプレイヤーを解決せず、実行時に [TargetAccountResolver] で解決する。
 * これにより、オフラインプレイヤーの名前とセレクタ（`@a` など）の両方を受け付けられる。
 *
 * @property input コマンドに入力された文字列（例: "_NIKOMARU", "@a[distance=..10]"）
 */
data class PlayerTarget(val input: String) {

    /** 入力がエンティティセレクタ（`@` で始まる）かどうか */
    val isSelector: Boolean
        get() = input.startsWith("@")
}
