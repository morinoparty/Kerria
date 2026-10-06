package party.morino.kerria.paper

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.leangen.geantyref.TypeToken
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.setting.ManagerSetting
import party.morino.kerria.paper.listener.AccountCreateListener
import party.morino.kerria.paper.commands.target.PlayerTarget
import party.morino.kerria.paper.commands.target.PlayerTargetParser
import org.incendo.cloud.annotations.AnnotationParser
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.dsl.module
import party.morino.kerria.api.KerriaAPI
import party.morino.kerria.api.account.AccountManager
import party.morino.kerria.api.account.BankManager
import party.morino.kerria.api.currency.CurrencyManager
import party.morino.kerria.api.economy.EconomyManager
import party.morino.kerria.api.files.ConfigManager
import party.morino.kerria.api.files.MessageManager
import party.morino.kerria.api.log.LogManager
import party.morino.kerria.common.account.AccountManagerImpl
import party.morino.kerria.paper.account.BankManagerImpl
import party.morino.kerria.paper.commands.AdminEconomyCommand
import party.morino.kerria.paper.commands.BalanceCommand
import party.morino.kerria.paper.commands.ConvertCommand
import party.morino.kerria.paper.commands.CurrencyCommand
import party.morino.kerria.paper.commands.LogCommand
import party.morino.kerria.paper.commands.PayCommand
import party.morino.kerria.paper.commands.ReloadCommand
import party.morino.kerria.paper.commands.TopCommand
import party.morino.kerria.common.currency.CurrencyManagerImpl
import party.morino.kerria.paper.database.DatabaseManager
import party.morino.kerria.common.database.repository.AccountRepository
import party.morino.kerria.common.database.repository.BankRepository
import party.morino.kerria.common.database.repository.CurrencyRepository
import party.morino.kerria.common.database.repository.ExchangeRateRepository
import party.morino.kerria.common.database.repository.TransactionLogRepository
import party.morino.kerria.api.economy.ExchangeRateManager
import party.morino.kerria.paper.economy.EconomyManagerImpl
import party.morino.kerria.paper.economy.ExchangeRateManagerImpl
import party.morino.kerria.paper.economy.VaultEconomy
import party.morino.kerria.paper.files.ConfigManagerImpl
import party.morino.kerria.paper.files.MessageManagerImpl
import party.morino.kerria.paper.integration.placeholder.KerriaExpansion
import party.morino.kerria.common.log.LogManagerImpl

/**
 * Kerriaプラグインのメインクラス
 *
 * DI設定、DB初期化、Vault登録を行うエントリーポイント。
 */
open class Kerria : SuspendingJavaPlugin(), KerriaAPI {

    private lateinit var accountManager: AccountManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var economyManager: EconomyManager
    private lateinit var logManager: LogManager
    private lateinit var bankManager: BankManager
    private lateinit var exchangeRateManager: ExchangeRateManager

    override suspend fun onEnableAsync() {
        // DI設定
        setupKoin()

        // マネージャーの取得
        accountManager = GlobalContext.get().get()
        currencyManager = GlobalContext.get().get()
        economyManager = GlobalContext.get().get()
        logManager = GlobalContext.get().get()
        bankManager = GlobalContext.get().get()
        exchangeRateManager = GlobalContext.get().get()

        // データベースの初期化
        val databaseManager: DatabaseManager = GlobalContext.get().get()
        databaseManager.initialize()

        // コマンドの登録
        registerCommands()

        // Kerria API サービスの登録（外部プラグイン向け）
        server.servicesManager.register<KerriaAPI>(
            KerriaAPI::class.java,
            this,
            this,
            ServicePriority.Normal,
        )

        // Vault Economy サービスの登録（Vault が存在する場合のみ）
        registerVaultEconomy()

        // 参加時のアカウント作成リスナーの登録
        server.pluginManager.registerEvents(AccountCreateListener(), this)

        // PlaceholderAPI 連携の登録（存在する場合のみ）
        registerPlaceholders()

        logger.info("${pluginMeta.name} v${pluginMeta.version} has been enabled!")
    }

    override suspend fun onDisableAsync() {
        // HikariCP接続プールを安全に閉じる
        val databaseManager: DatabaseManager = GlobalContext.get().get()
        databaseManager.shutdown()
        logger.info("${pluginMeta.name} has been disabled!")
    }

    /**
     * Koin DI コンテナの初期化
     */
    private fun setupKoin() {
        val appModule = module {
            // プラグイン本体
            single<Kerria> { this@Kerria }
            single<KerriaAPI> { this@Kerria }

            // 設定
            single<ConfigManager> { ConfigManagerImpl(this@Kerria) }

            // メッセージ
            single<MessageManager> { MessageManagerImpl(this@Kerria) }

            // リポジトリ
            single { AccountRepository() }
            single { CurrencyRepository() }
            single { TransactionLogRepository() }
            single { ExchangeRateRepository() }
            single { BankRepository() }

            // DB管理
            single { DatabaseManager(this@Kerria) }

            // マネージャー
            single<AccountManager> { AccountManagerImpl() }
            single<CurrencyManager> { CurrencyManagerImpl() }
            single<LogManager> { LogManagerImpl() }
            single<EconomyManager> { EconomyManagerImpl() }
            single<ExchangeRateManager> { ExchangeRateManagerImpl() }
            single<BankManager> { BankManagerImpl() }
        }

        // 既存の Koin がある場合はモジュールを追加、なければ新規開始
        val koin = getOrNull()
        if (koin != null) {
            koin.loadModules(listOf(appModule))
        } else {
            GlobalContext.startKoin {
                modules(appModule)
            }
        }
    }

    /**
     * Vault が存在する場合に Kerria の Economy を Vault のサービスとして登録する
     *
     * Vault は任意依存のため、未導入の環境では Vault のクラスを読み込まないようガードする。
     */
    private fun registerVaultEconomy() {
        if (server.pluginManager.getPlugin("Vault") == null) {
            logger.info("Vault is not installed. Skipping Vault economy registration.")
            return
        }
        server.servicesManager.register<Economy>(
            Economy::class.java,
            VaultEconomy(),
            this,
            ServicePriority.Highest,
        )
    }

    /**
     * PlaceholderAPI が存在する場合に Kerria の Expansion を登録する
     *
     * PlaceholderAPI は任意依存（softdepend）のため、未導入でもプラグインは正常に動作する。
     */
    private fun registerPlaceholders() {
        // PlaceholderAPI が無い環境では KerriaExpansion をクラスロードしないよう、
        // 参照はこのガードの内側に閉じ込める
        if (!server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            return
        }
        try {
            KerriaExpansion().register()
            logger.info("Registered PlaceholderAPI expansion.")
        } catch (e: Exception) {
            // 連携の失敗は致命的ではないため、警告のみで続行する
            logger.warning("Failed to register PlaceholderAPI expansion: ${e.message}")
        }
    }

    override fun getAccountManager(): AccountManager = accountManager
    override fun getCurrencyManager(): CurrencyManager = currencyManager
    override fun getEconomyManager(): EconomyManager = economyManager
    override fun getLogManager(): LogManager = logManager
    override fun getBankManager(): BankManager = bankManager
    override fun getExchangeRateManager(): ExchangeRateManager = exchangeRateManager

    /**
     * [PlayerTarget] のパーサーを Cloud と Brigadier に登録する
     *
     * Brigadier 上では Paper のプレイヤーセレクタ引数として扱うことで、`@a` などの入力が
     * Brigadier の解析で拒否されず、クライアントでもセレクタの補完が表示されるようにする。
     * 実際の値の解析とプレイヤーの解決は Cloud 側（[PlayerTargetParser]）で行う。
     */
    @Suppress("UnstableApiUsage")
    private fun registerPlayerTargetParser(commandManager: PaperCommandManager<CommandSourceStack>) {
        commandManager.parserRegistry().registerParserSupplier(TypeToken.get(PlayerTarget::class.java)) {
            PlayerTargetParser()
        }
        if (commandManager.hasBrigadierManager()) {
            commandManager.brigadierManager().registerMapping(
                object : TypeToken<PlayerTargetParser<CommandSourceStack>>() {},
            ) { builder -> builder.toConstant(ArgumentTypes.players()).nativeSuggestions() }
        }
    }

    /**
     * Cloud のコマンドマネージャーを作成する
     */
    @Suppress("UnstableApiUsage")
    private fun createCommandManager(): PaperCommandManager<CommandSourceStack> =
        PaperCommandManager
            .builder()
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildOnEnable(this)
            .also { manager ->
                // 省略可能な引数（currencyId）を省略したままフラグ（--message）を指定できるようにする
                manager.settings().set(ManagerSetting.LIBERAL_FLAG_PARSING, true)
            }

    /**
     * Cloud Annotations を使ってコマンドを登録する
     */
    private fun registerCommands() {
        // テスト環境(MockBukkit)では Bootstrap が実行されず、コマンドマネージャーを作成できないため登録を省略する
        if (!KerriaBootstrap.bootstrapped) {
            logger.warning("Plugin was not bootstrapped. Skipping command registration.")
            return
        }
        // Bootstrap で作成したマネージャーは onEnable の時点で登録を受け付けなくなるため、onEnable で作成する
        val commandManager = createCommandManager()

        // 対象プレイヤー（名前またはセレクタ）のパーサーを登録する
        registerPlayerTargetParser(commandManager)

        @Suppress("UnstableApiUsage")
        val annotationParser = AnnotationParser(
            commandManager,
            CommandSourceStack::class.java,
        )
        annotationParser.parse(
            BalanceCommand(),
            PayCommand(),
            AdminEconomyCommand(),
            TopCommand(),
            LogCommand(),
            CurrencyCommand(),
            ConvertCommand(),
            ReloadCommand(),
        )
    }
}
