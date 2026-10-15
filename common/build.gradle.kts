plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    // 公開APIの型・共有ライブラリを依存先（paper / velocity）へ推移的に公開する
    api(project(":api"))
    api(libs.arrow.core)

    // データベース層（Exposed + ドライバ + HikariCP）
    api(libs.bundles.database)

    // 設定ファイル（HOCON）の読み書き。Paper / Velocity の両方で同じローダーを使う
    api(libs.bundles.hocon)

    // MiniPlaceholders のプレースホルダー定義（実行時は MiniPlaceholders と各プラットフォームが提供する）
    compileOnly(libs.miniplaceholders.api)
    compileOnly(libs.adventure.text.minimessage)

    // DI（by inject を利用する実装のため）
    api(libs.koin.core)
}
