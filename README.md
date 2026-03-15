# Kerria

morinoparty の Minecraft 経済プラグインです。Paper / Velocity 両対応のマルチモジュール構成になっています。

## モジュール構成

| モジュール | 説明 |
|-----------|------|
| `common` | Paper/Velocity 共通コード |
| `paper` | Paper (Bukkit) プラグイン |
| `velocity` | Velocity プロキシプラグイン |
| `api` | 外部プラグイン向け公開 API |

## 技術スタック

- **Kotlin** - 言語
- **Paper API** 1.21.8 - Minecraft サーバー API
- **Velocity API** 3.4.0 - Minecraft プロキシ API
- **Cloud** - コマンドフレームワーク (Incendo)
- **Koin** - 依存性注入
- **MCCoroutine** - Kotlin Coroutines の Minecraft 統合
- **ShadowJar** - Fat JAR 生成
- **Fumadocs** - ドキュメントサイト (Next.js)

## 必要環境

- **Java** 21 (Temurin 推奨)
- **Gradle** 9.x (Wrapper 同梱)
- **Node.js** 22+ / **pnpm** 10+ (ドキュメントビルド用)
- **[Task](https://taskfile.dev/)** (タスクランナー、任意)

## ビルド

```bash
# Gradle ビルド
./gradlew build -x test

# または Task を使用
task build
```

## 開発サーバー起動

```bash
# Paper テストサーバー
./gradlew :paper:runServer

# または Task を使用
task run
```

## ドキュメント開発

```bash
cd docs
pnpm install
pnpm dev

# または Task を使用
task docs
```

## Task コマンド一覧

| コマンド | 説明 |
|---------|------|
| `task build` | 全モジュールをビルド |
| `task run` | Paper 開発サーバーを起動 |
| `task docs` | ドキュメント開発サーバーを起動 |
| `task check` | フォーマット + ビルド |
| `task clear` | session.lock ファイルを削除 |

## GitHub Actions

| ワークフロー | トリガー | 説明 |
|-------------|---------|------|
| `check_pull_request.yml` | Pull Request | ビルドチェック |
| `preview.yml` | Pull Request | プレビュービルド・S3 アップロード・PR コメント |
| `upload.yml` | Release published | GitHub Release にJAR をアップロード |
| `release.yml` | Push to main | Release Drafter でドラフトリリース作成 |
| `deploy_docs.yml` | Push to main (docs/) | GitHub Pages にドキュメントデプロイ |
| `dependabot_auto_merge.yml` | Dependabot PR | 自動マージ |
| `sync-label.yml` | labels.json 変更 | GitHub ラベル同期 |

## ライセンス

CC0-1.0
