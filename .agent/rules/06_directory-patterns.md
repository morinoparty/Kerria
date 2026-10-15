## ディレクトリ配置規則

- docs/content/ にドキュメントを配置します。
- common/src/main/kotlin/party/morino/kerria/common/ に共通コードを配置します。
- paper/src/main/kotlin/party/morino/kerria/paper/ にPaperプラグインのコードを配置します。
- velocity/src/main/kotlin/party/morino/kerria/velocity/ にVelocityプラグインのコードを配置します。
- api/src/main/kotlin/party/morino/kerria/api/ にAPIのインターフェースを配置します。
- 機能ごとのコードは、各モジュール配下の機能名のパッケージ (例: `account`, `currency`, `economy`, `log`) に配置します。
- data classはそれぞれのmodelに配置します。
