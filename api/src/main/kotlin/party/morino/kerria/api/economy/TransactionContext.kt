package party.morino.kerria.api.economy

/**
 * 取引に付けるメッセージを、呼び出し元のスレッドに一時的に設定するためのコンテキスト
 *
 * Vault 経由の取引（`depositPlayer` / `withdrawPlayer` など）にはメッセージを渡す引数が無いため、
 * ショッププラグインのアドオンなどが「この後の取引は何のための取引か」をここに設定しておくと、
 * Kerria が取引ログを記録するときにそのメッセージを付ける。
 *
 * メッセージはスレッドごとに保持されるため、別スレッドで行われる他のプラグインの取引には付かない。
 * メッセージはプレーンテキストとして扱われ、MiniMessage のタグは解釈されない。
 *
 * ```kotlin
 * // ブロック内で行われた取引にメッセージを付ける
 * TransactionContext.withMessage("ダイヤモンド ×16 を購入") {
 *     economy.withdrawPlayer(player, 100.0)
 * }
 * ```
 *
 * イベントをまたいで処理が進む場合（取引前イベントで設定し、取引後イベントで解除する等）は、
 * [setMessage] と [clear] を使う。解除し忘れると後続の取引にもメッセージが付くため、必ず [clear] を呼ぶこと。
 */
object TransactionContext {
    // スレッドごとのメッセージ（未設定なら null）
    private val currentMessage = ThreadLocal<String?>()

    /**
     * 現在のスレッドに設定されているメッセージを取得する
     *
     * @return 設定されているメッセージ。未設定なら null
     */
    @JvmStatic
    fun currentMessage(): String? = currentMessage.get()

    /**
     * 現在のスレッドで以降に行われる取引のメッセージを設定する
     *
     * @param message 取引に付けるメッセージ（プレーンテキスト）
     */
    @JvmStatic
    fun setMessage(message: String) {
        currentMessage.set(message)
    }

    /**
     * 現在のスレッドに設定されているメッセージを解除する
     */
    @JvmStatic
    fun clear() {
        currentMessage.remove()
    }

    /**
     * ブロックの実行中だけメッセージを設定する
     *
     * ブロックの終了後は、入れ子で呼ばれた場合も含めて元のメッセージに戻す。
     *
     * @param message 取引に付けるメッセージ（プレーンテキスト）
     * @param block メッセージを付けたい取引を行う処理
     * @return ブロックの戻り値
     */
    @JvmStatic
    fun <T> withMessage(message: String, block: () -> T): T {
        val previous = currentMessage.get()
        currentMessage.set(message)
        try {
            return block()
        } finally {
            // 入れ子の呼び出しにも対応するため、解除ではなく元の値へ戻す
            if (previous == null) currentMessage.remove() else currentMessage.set(previous)
        }
    }
}
