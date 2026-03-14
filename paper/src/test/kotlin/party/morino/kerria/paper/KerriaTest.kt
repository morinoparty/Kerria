package party.morino.kerria.paper

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

/**
 * テスト用の JUnit 5 拡張クラス
 * MockBukkit と Koin の初期化・クリーンアップを行う
 */
class KerriaTest :
    BeforeAllCallback,
    AfterAllCallback {

    companion object {
        lateinit var server: ServerMock
        lateinit var plugin: Kerria
    }

    override fun beforeAll(context: ExtensionContext) {
        // MockBukkit サーバーを初期化
        server = MockBukkit.mock()

        // テスト用の Koin モジュールを設定
        val appModule = module {
            single<ServerMock> { server }
        }

        // Koin が未初期化の場合のみ開始
        getOrNull() ?: GlobalContext.startKoin {
            modules(appModule)
        }

        // プラグインをロード
        plugin = MockBukkit.load(Kerria::class.java)

        // プラグインインスタンスを Koin に登録
        GlobalContext.get().declare(plugin)
    }

    override fun afterAll(context: ExtensionContext) {
        MockBukkit.unmock()
        GlobalContext.stopKoin()
    }
}
