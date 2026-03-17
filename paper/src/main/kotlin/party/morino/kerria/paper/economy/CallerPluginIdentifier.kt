package party.morino.kerria.paper.economy

import org.bukkit.Bukkit

/**
 * 呼び出し元プラグインを特定するユーティリティ
 *
 * StackWalker を使ってコールスタックを辿り、
 * Vault API 経由で操作を実行した外部プラグインを特定する。
 */
object CallerPluginIdentifier {

    // Kerria自身のClassLoaderを保持
    private val kerriaClassLoader: ClassLoader = CallerPluginIdentifier::class.java.classLoader

    /**
     * 呼び出し元のプラグイン名を特定する
     *
     * コールスタック上のClassLoaderを、登録済みプラグインのClassLoaderと照合し、
     * Kerria以外で最初に一致したプラグインの名前を返す。
     *
     * @return 特定できたプラグイン名、特定できない場合は null
     */
    fun identify(): String? {
        // 現在登録されている全プラグインのClassLoaderとプラグイン名のマッピング
        val pluginsByClassLoader = Bukkit.getPluginManager().plugins
            .associate { it.javaClass.classLoader to it.name }

        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk { frames ->
                frames
                    // 各フレームのClassLoaderを取得
                    .map { frame -> frame.declaringClass.classLoader }
                    // Kerria自身のClassLoaderは除外
                    .filter { it != kerriaClassLoader }
                    // プラグインのClassLoaderに一致するプラグイン名を取得
                    .map { pluginsByClassLoader[it] }
                    // null（プラグイン外のフレーム）を除外
                    .filter { it != null }
                    // 最初に見つかったプラグイン名を返す
                    .findFirst()
                    .orElse(null)
            }
    }
}
