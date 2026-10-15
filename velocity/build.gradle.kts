plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resource.factory)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))

    compileOnly(libs.velocity.api)
    // MiniPlaceholders 連携（存在する場合のみ利用する任意依存。実行時は MiniPlaceholders が提供する）
    compileOnly(libs.miniplaceholders.api)

    implementation(libs.kotlinx.serialization.json)
}

tasks {
    build {
        dependsOn("shadowJar")
    }
    shadowJar
}

sourceSets.main {
    resourceFactory {
        velocityPluginJson {
            id.set("kerria")
            name.set(rootProject.name)
            version.set(project.version.toString())
            description.set("Cross-server economy support for Kerria")
            url.set("https://github.com/morinoparty/Kerria")
            authors.add("morinoparty")
            main.set("party.morino.kerria.velocity.KerriaVelocity")
            // MiniPlaceholders は任意依存（存在すればプレースホルダーを登録する）
            dependency("miniplaceholders", true)
        }
    }
}
