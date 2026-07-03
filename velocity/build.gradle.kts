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

    implementation(libs.kaml)
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
        }
    }
}
