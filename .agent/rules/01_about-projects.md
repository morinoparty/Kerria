## このアプリケーションの概要
「Kerria」という、morinoparty 向けの経済 (マネー管理) Minecraft プラグインです。
リポジトリ名は `Kerria` です。

## 目的
Paper サーバーと Velocity プロキシで 1 つのデータベースを共有し、複数サーバーをまたいで使える経済機能を提供します。
現在、以下の機能があります。

- **account** - プレイヤーごとのアカウントと、通貨ごとの残高
- **currency** - 複数通貨の管理 (`/kerria currency`) と、通貨間の為替レート・両替 (`/convert`)
- **bank** - 複数人で共有できる銀行口座
- **log** - すべての取引の履歴 (取引メッセージ付き) と、古いログの削除
- **integration** - Vault (Economy API) と PlaceholderAPI への連携
- **cross-server** - Velocity プロキシ上の `/vkerria` コマンドによる、サーバーをまたいだ残高確認・送金

### 設計上の前提
- 通貨の情報 (名前・記号・複数形・フォーマット・桁数など) はデータベースだけで管理します。設定ファイルには通貨の情報を持たせないでください。
- 設定ファイルは HOCON 形式の `config.conf` で、キーは kebab-case です。既定ファイルは `kerria/config.conf` リソースとして同梱します。
- Paper のコマンドは `/kerria`、Velocity のコマンドは `/vkerria` です。プロキシに同名のコマンドを登録すると Paper 側のコマンドが実行できなくなるため、ラベルを共有しないでください。
- アカウントID などの Kerria が採番する ID は UUID v7、プレイヤーの UUID は Minecraft (Mojang) の UUID v4 です。両者を混同しないでください。

## 主な技術スタック
- Kotlin (言語)
- Koin (依存性注入)
- PaperMC (MinecraftプラグインAPI)
- Velocity (MinecraftプロキシAPI)
- Cloud (コマンドフレームワーク)
- MCCoroutine (非同期処理)
- Exposed (データベースアクセス。SQLite / PostgreSQL、PostgreSQL では HikariCP で接続をプールする)
- kotlinx.serialization (設定ファイルの読み込み。HOCON)
- Arrow (`Either` によるエラーハンドリング)

## モジュール構成
- `common` - Paper/Velocity共通コード (データベース、設定ファイルの読み込みなど)
- `paper` - Paper (Bukkit) プラグイン
- `velocity` - Velocity プロキシプラグイン
- `api` - 外部プラグイン向け公開API
