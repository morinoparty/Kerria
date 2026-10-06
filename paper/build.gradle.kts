import java.time.Duration
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))
    compileOnly(libs.paper.api)

    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    implementation(libs.bundles.commands.paper)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
    implementation(libs.bundles.coroutines.bukkit)

    // i18n（翻訳）: Adventure の MiniMessageTranslationStore を利用する（実行時は Paper が提供）
    compileOnly(libs.adventure.text.minimessage)
    testImplementation(libs.adventure.text.minimessage)

    implementation(libs.bundles.database)

    // JARにバンドル
    implementation(libs.koin.core)

    compileOnly(libs.vault.api)
    // PlaceholderAPI 連携（存在する場合のみ利用する softdepend）
    compileOnly(libs.placeholderapi)

    // テスト依存関係
    testImplementation(libs.paper.api)
    testImplementation(libs.vault.api)
    testImplementation(libs.placeholderapi)
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.bundles.koin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mock.bukkit)
    testImplementation(libs.allure.junit5)
}

tasks {
    build {
        dependsOn("shadowJar")
    }
    shadowJar
    test {
        useJUnitPlatform()
        // Allure結果の出力先を指定
        systemProperty("allure.results.directory", "${project.layout.buildDirectory.get().asFile}/allure-results")
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
    runServer {
        minecraftVersion("26.2")
        val plugins = runPaper.downloadPluginsSpec {
            url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
        }
        downloadPlugins {
            downloadPlugins.from(plugins)
        }
    }
}

testing {
    suites {
        // ゲーム内テスト（fukurou）は Minecraft のクライアントと Xvfb が要るため、既定の test（./gradlew build が実行する）とは分ける。
        // 独自の JvmTestSuite は check に含まれないので、build では実行されない
        register<JvmTestSuite>("gameTest") {
            // fukurou v3 は JUnit 6 を前提にしている
            useJUnitJupiter(libs.versions.junit)
            dependencies {
                implementation(libs.fukurou)
                // JUnit 6 が suspend のテストメソッドを呼ぶには kotlinx-coroutines-core が要る
                implementation(libs.kotlinx.coroutines.core)
                runtimeOnly(libs.junit.platform.launcher)
            }
            targets.configureEach {
                testTask.configure {
                    description = "Runs the in-game tests with fukurou (needs Xvfb, xdotool, xmodmap and Mesa; CI only)"
                    // CI は build ジョブの JAR を -Pfukurou.plugin.kerria で渡す。無ければここで shadowJar を作る
                    val prebuilt = providers.gradleProperty("fukurou.plugin.kerria")
                    if (!prebuilt.isPresent) dependsOn(tasks.shadowJar)
                    val pluginJar =
                        prebuilt.orElse(tasks.shadowJar.flatMap { it.archiveFile }.map { it.asFile.absolutePath })
                    // -Pfukurou.* をすべてシステムプロパティとして渡す（minecraftVersion, paperChannel, acceptEula, outDir …）
                    val forwarded = providers.gradlePropertiesPrefixedBy("fukurou.")
                    jvmArgumentProviders.add(
                        CommandLineArgumentProvider {
                            forwarded.get().filterKeys { it != "fukurou.plugin.kerria" }.map { (k, v) -> "-D$k=$v" } +
                                "-Dfukurou.plugin.kerria=${pluginJar.get()}"
                        },
                    )
                    // 出力先と作業ディレクトリの既定値（-Pfukurou.outDir / FUKUROU_OUT_DIR があればそちらが優先される）
                    systemProperty("fukurou.outDir.default", layout.buildDirectory.dir("fukurou/out").get().asFile.absolutePath)
                    systemProperty("fukurou.workDir.default", layout.buildDirectory.dir("fukurou/work").get().asFile.absolutePath)
                    // サーバーのリースとメモリ予算は 1 つの JVM を前提にしている
                    maxParallelForks = 1
                    forkEvery = 0
                    maxHeapSize = "512m"
                    // 実機テストは入力が同じでも結果が変わり、バージョンなどは環境変数で渡るので、
                    // UP-TO-DATE にもビルドキャッシュ（gradle.properties で有効）からの復元にもしない
                    outputs.upToDateWhen { false }
                    outputs.cacheIf { false }
                    // CI の timeout-minutes（30）より短くし、強制終了の前に JUnit の XML と result.json を書き終える
                    timeout.set(Duration.ofMinutes(25))
                    testLogging {
                        showStandardStreams = true
                        events("passed", "skipped", "failed")
                        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                    }
                }
            }
        }
    }
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = rootProject.name
            version = project.version.toString()
            website = "https://github.com/morinoparty/Kerria"
            main = "$group.kerria.paper.Kerria"
            bootstrapper = "$group.kerria.paper.KerriaBootstrap"
            loader = "$group.kerria.paper.KerriaLoader"
            apiVersion = "26.1"
            // PlaceholderAPI は任意依存（存在すれば連携する）
            dependencies {
                server("PlaceholderAPI", PaperPluginYaml.Load.BEFORE, required = false)
                // Vault は任意依存（存在すれば Economy を登録する）。Vault のクラスを参照するためクラスパスを共有する
                server("Vault", PaperPluginYaml.Load.BEFORE, required = false, joinClasspath = true)
            }
        }
    }
}
