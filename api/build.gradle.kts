import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    `maven-publish`
}

group = project.group
version = project.version.toString()

dokka {
    pluginsConfiguration.html {
        footerMessage.set("No right reserved. This docs under CC0 1.0.")
    }
    dokkaPublications.html {
        outputDirectory.set(file("${project.rootDir}/docs/static/dokka"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "kerria.api"
            version = version
            from(components["kotlin"])
        }
    }
}

dependencies{
    compileOnly(libs.paper.api)

    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)
}
kotlin {
    jvmToolchain {
        (this).languageVersion.set(JavaLanguageVersion.of(21))
    }
    jvmToolchain(21)
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        compilerOptions.javaParameters = true
    }
}
repositories {
    mavenCentral()
}