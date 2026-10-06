package party.morino.kerria.paper.files

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import party.morino.kerria.api.files.Config
import party.morino.kerria.common.files.HoconConfigLoader
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * HOCON 設定ローダーのテスト
 */
class HoconConfigLoaderTest {

    // テスト用クラスパスに含まれる Paper の既定ファイルを使う
    private val loader = HoconConfigLoader(javaClass.classLoader, "kerria/config.conf")

    @Test
    @DisplayName("Missing file is created from the commented default resource")
    fun createsDefaultFile(@TempDir dir: File) {
        val file = File(dir, "config.conf")
        val config = loader.load(file, Config.serializer())

        // 既定値で読み込まれ、リソースの原文（コメント付き）がそのまま書き出される
        assertEquals(Config(), config)
        assertTrue(file.readText().contains("# デバッグモード"))
    }

    @Test
    @DisplayName("Missing keys fall back to defaults and kebab-case keys are read")
    fun fallsBackToDefaults(@TempDir dir: File) {
        val file = File(dir, "config.conf").apply {
            writeText("economy { initial-balance = 100.5 }\ndatabase.pool.maximum-pool-size = 3\n")
        }
        val config = loader.load(file, Config.serializer())

        // 書いたキーだけが反映され、それ以外は既定値になる
        assertEquals(100.5, config.economy.initialBalance)
        assertEquals(1, config.economy.defaultCurrencyId)
        assertEquals(3, config.database.pool.maximumPoolSize)
        assertEquals("sqlite", config.database.mode)
    }

    @Test
    @DisplayName("Save rewrites only changed lines and keeps comments and substitutions")
    fun saveRewritesOnlyChangedLines(@TempDir dir: File) {
        val file = File(dir, "config.conf")
        loader.load(file, Config.serializer())
        // 環境変数による置換を含む行を用意する（未設定なので既定値の password が使われる）
        file.writeText(file.readText().replace("password = \"password\"", "password = \"password\"\n  password = ${'$'}{?KERRIA_TEST_UNSET_ENV}"))
        val before = file.readText()

        val config = loader.load(file, Config.serializer())
        loader.save(file, Config.serializer(), config, config.copy(economy = config.economy.copy(defaultCurrencyId = 5)))

        // 変更した 1 行以外（コメントや置換式）はそのまま残る
        val after = file.readText()
        assertEquals(before.replace("default-currency-id = 1", "default-currency-id = 5"), after)
        assertEquals(5, loader.load(file, Config.serializer()).economy.defaultCurrencyId)
    }

    @Test
    @DisplayName("Save appends an override when the key is not written in the file")
    fun saveAppendsMissingKey(@TempDir dir: File) {
        val file = File(dir, "config.conf").apply { writeText("debug = true\n") }
        val config = loader.load(file, Config.serializer())
        loader.save(file, Config.serializer(), config, config.copy(economy = config.economy.copy(defaultCurrencyId = 7)))

        // ファイルに無いキーは末尾に追記され、読み直すと反映されている
        assertTrue(file.readText().endsWith("economy.default-currency-id = 7\n"))
        val reloaded = loader.load(file, Config.serializer())
        assertEquals(7, reloaded.economy.defaultCurrencyId)
        assertTrue(reloaded.debug)
    }

    @Test
    @DisplayName("Save keeps unrelated edits made to the file after loading")
    fun saveKeepsOutOfBandEdits(@TempDir dir: File) {
        val file = File(dir, "config.conf")
        val config = loader.load(file, Config.serializer())
        // 読み込み後にファイルだけが手で編集された状態を作る
        file.writeText(file.readText().replace("host = \"localhost\"", "host = \"db.example\""))

        loader.save(file, Config.serializer(), config, config.copy(economy = config.economy.copy(defaultCurrencyId = 2)))

        // メモリ上の古い値で巻き戻さず、変更したキーだけが書き込まれる
        val reloaded = loader.load(file, Config.serializer())
        assertEquals("db.example", reloaded.database.host)
        assertEquals(2, reloaded.economy.defaultCurrencyId)
    }

    @Test
    @DisplayName("Save does not break lines that hold several keys")
    fun saveHandlesCompactLines(@TempDir dir: File) {
        val file = File(dir, "config.conf").apply {
            writeText("economy { default-currency-id = 1, initial-balance = 50 }\n")
        }
        val config = loader.load(file, Config.serializer())
        loader.save(file, Config.serializer(), config, config.copy(economy = config.economy.copy(defaultCurrencyId = 4)))

        // 同じ行の別のキーや閉じ括弧を消さずに、変更が反映される
        val reloaded = loader.load(file, Config.serializer())
        assertEquals(4, reloaded.economy.defaultCurrencyId)
        assertEquals(50.0, reloaded.economy.initialBalance)
    }
}
