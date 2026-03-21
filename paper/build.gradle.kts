plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory)
    alias(libs.plugins.allure)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    implementation(project(":api"))
    compileOnly(libs.paper.api)

    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    implementation(libs.bundles.commands.paper)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
    implementation(libs.bundles.coroutines.bukkit)

    implementation(libs.bundles.database)

    // JARにバンドル
    implementation(libs.koin.core)

    compileOnly(libs.vault.api)

    // テスト依存関係
    testImplementation(libs.paper.api)
    testImplementation(libs.vault.api)
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
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
    runServer {
        minecraftVersion("1.21.8")
        val plugins = runPaper.downloadPluginsSpec {
            url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
        }
        downloadPlugins {
            downloadPlugins.from(plugins)
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
            apiVersion = "1.21"
        }
    }
}
