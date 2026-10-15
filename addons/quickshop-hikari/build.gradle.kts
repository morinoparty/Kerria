import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.resource.factory)
}

group = "party.morino"
version = project.version.toString()

// アドオン名（プラグイン名は Kerria-addon-quickshop-hikari になる）
val addonName = "addon-quickshop-hikari"

dependencies {
    // Kerria 本体が実行時に提供する（JAR には同梱しない）
    compileOnly(project(":api"))
    compileOnly(libs.paper.api)
    // QuickShop-Hikari 本体が実行時に提供する
    compileOnly(libs.quickshop.api)

    // Kotlin 標準ライブラリはアドオンのローダーが実行時に取得する（JAR には同梱しない）
    implementation(kotlin("stdlib"))

    testImplementation(libs.paper.api.test)
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(kotlin("test"))
}

// アドオンのローダーが実行時に取得するライブラリ一覧（Kotlin 標準ライブラリのみ）
val runtimeLibraries =
    configurations.runtimeClasspath.map { configuration ->
        configuration.incoming.artifacts.artifacts
            .mapNotNull { it.id.componentIdentifier as? ModuleComponentIdentifier }
            .map { "${it.group}:${it.module}:${it.version}" }
            .distinct()
    }

// QuickShopAddonLoader が読み込むライブラリ一覧をリソースとして生成する
val generateAddonLibraries by tasks.registering {
    val libraries = runtimeLibraries
    val outputDirectory = layout.buildDirectory.dir("generated/addon-libraries")
    inputs.property("libraries", libraries)
    outputs.dir(outputDirectory)
    doLast {
        outputDirectory.get().file("addon-libraries.txt").asFile.writeText(libraries.get().joinToString("\n"))
    }
}

sourceSets.main {
    resources.srcDir(generateAddonLibraries)
    resourceFactory {
        paperPluginYaml {
            name = rootProject.name + "-" + addonName
            version = project.version.toString()
            website = "https://github.com/morinoparty/Kerria"
            main = "$group.kerria.addons.quickshop.QuickShopHikariAddon"
            loader = "$group.kerria.addons.quickshop.QuickShopAddonLoader"
            apiVersion = "26.1"
            description = "QuickShop-Hikari の取引内容を Kerria の取引ログに記録するアドオン"
            dependencies {
                // Kerria の API（TransactionContext）を参照するため、クラスパスを共有する
                server("Kerria", PaperPluginYaml.Load.BEFORE, required = true, joinClasspath = true)
                // QuickShop のイベントを購読するため、クラスパスを共有する
                server("QuickShop-Hikari", PaperPluginYaml.Load.BEFORE, required = true, joinClasspath = true)
            }
        }
    }
}
