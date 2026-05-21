# タルムードに学ぶ ― 賢者の金融市場 (TalmudFinance)

金融マーケットの主要指標を表示しつつ、毎日タルムードの教えを1つ提示するAndroidアプリ。

## コンセプト
- **古代の知恵 × 現代の市場**: 2000年前のユダヤの賢者の言葉を、現代の投資判断に応用する。
- **静かな日課**: 毎朝、今日の教えと主要指標を1画面で確認できる。

## アーキテクチャ（MVP）

```
Jetpack Compose (UI)
    ↓
ViewModel + StateFlow
    ↓
Repository  ──→  Yahoo Finance API (株/為替/暗号資産)
           └─→  assets/talmud_teachings.json (教え)
```

- **言語**: Kotlin 1.9.24
- **UI**: Jetpack Compose (Material3)
- **非同期**: Kotlin Coroutines
- **通信**: Retrofit + OkHttp + kotlinx.serialization
- **min SDK / target SDK**: 24 / 34

## 画面構成
1. **今日**（Home）: 今日のタルムードカード + 主要4指標サマリー
2. **マーケット**（Market）: 日本株 / 米国株 / 為替 / 暗号資産 のタブ別一覧
3. **教え一覧**（Talmud）: 100件のタルムードの教えと金融的解釈

## データソース

### マーケットデータ
**Yahoo Finance Chart API（非公式・無料・APIキー不要）**を使用。

- 例: `https://query1.finance.yahoo.com/v8/finance/chart/^N225`
- 採用銘柄は `MarketRepository.kt` の `watchlist` で定義（19銘柄）
- 注意: 非公式エンドポイントのため、本番リリース前に **Alpha Vantage / Twelve Data / 各証券会社の公式API** への切り替えを推奨

### タルムードの教え
`app/src/main/assets/talmud_teachings.json` に **100件**（Phase 2 ゲート達成）。確定出典 55件、(趣旨) 29件、伝統的格言 16件。

- 「日付」から決定論的に選び、同じ日には同じ教えを表示
- JSONを編集すれば即時に反映される（ビルド時に取り込まれる）
- 出典は伝統文献の趣旨に基づく日本語意訳。学術的に正確な引用に差し替える場合は本ファイルを編集してください

## セットアップ

### 必要環境
- Android Studio Hedgehog (2023.1.1) 以降
- JDK 17
- Android SDK 34
- Gradle 8.5+（Android Studioが自動で取得）

### 手順
1. Android Studio で `TalmudFinance` フォルダを開く（`File → Open`）
2. 初回は Gradle Sync が自動実行される（数分かかる場合あり）
3. 実機またはエミュレータを接続し、`Run > Run 'app'`（Shift + F10）
4. 初回起動時、ネットワーク権限が許可されていれば自動でマーケットデータを取得

### Gradle Wrapper
本プロジェクトには `gradle/wrapper/gradle-wrapper.jar` を同梱していません。
Android Studio で初回オープン時に「Gradle Wrapper を作成しますか？」と聞かれるので Yes を選択するか、
ターミナルで以下を実行してください:

```bash
gradle wrapper --gradle-version 8.7
```

## プロジェクト構成

```
TalmudFinance/
├── settings.gradle.kts
├── build.gradle.kts                ← ルート
├── gradle.properties
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── assets/
        │   └── talmud_teachings.json   ← 教えデータ（編集可）
        ├── java/com/talmudfinance/app/
        │   ├── TalmudFinanceApp.kt     ← Application（簡易DI）
        │   ├── MainActivity.kt
        │   ├── data/
        │   │   ├── model/              ← MarketData, TalmudTeaching
        │   │   ├── remote/             ← YahooFinanceApi, ApiClient
        │   │   └── repository/         ← MarketRepository, TalmudRepository
        │   └── ui/
        │       ├── theme/              ← Color, Type, Theme
        │       ├── components/         ← QuoteRow, LoadingBox, ErrorBox
        │       ├── home/               ← HomeScreen + ViewModel
        │       ├── market/             ← MarketScreen + ViewModel
        │       ├── talmud/             ← TalmudScreen + ViewModel
        │       └── navigation/         ← AppNavigation
        └── res/
            ├── values/                 ← strings, colors, themes
            ├── values-night/           ← ダークテーマ
            ├── drawable/               ← ic_launcher_*
            ├── mipmap-anydpi-v26/      ← adaptive launcher icon
            └── xml/                    ← backup_rules
```

## 既知の制約・今後の改善候補

| 区分 | 内容 |
|------|------|
| API | Yahoo Finance 非公式APIは突然仕様変更される可能性あり |
| 通知 | 「毎朝決まった時刻にプッシュ通知」は未実装。WorkManager + NotificationCompat で追加可能 |
| ウォッチリスト | 銘柄リストはハードコード。今後 DataStore でユーザー編集可能化を推奨 |
| チャート | 価格のみ表示。日足チャートは MPAndroidChart などで追加可能 |
| キャッシュ | 起動毎に再取得。Room or DataStore で前回値キャッシュを追加すると体験向上 |
| 多言語 | 日本語のみ。`values-en/strings.xml` 追加で対応可能 |
| 暗号資産 (JPY) | `BTC-JPY` は対応取引所が限られるためデータが入らないことがある |

## 教えの拡張方法

`app/src/main/assets/talmud_teachings.json` の `teachings` 配列に同形式で追記するだけ。

```json
{
  "id": 31,
  "teaching": "...",
  "source": "...",
  "lesson": "...",
  "financial_interpretation": "..."
}
```

教えの数を増やしても、日付ベースの剰余で自動的に循環します。

## プロジェクト運営ドキュメント

Phase 2 以降のチーム運営・タスク管理は `docs/` 配下のMarkdownで行います:

- [docs/TEAM.md](./docs/TEAM.md) — ロール設計・責任マトリクス・コミュニケーション設計
- [docs/ROADMAP.md](./docs/ROADMAP.md) — フェーズ別ロードマップとゲート条件
- [docs/BACKLOG.md](./docs/BACKLOG.md) — 優先度付きタスクバックログ
- [docs/KPI.md](./docs/KPI.md) — 北極星指標と週次レビュー記録
- [docs/RELEASE_CHECKLIST.md](./docs/RELEASE_CHECKLIST.md) — Google Play 申請前後のチェックリスト

## ライセンス
個人開発・学習目的のサンプルプロジェクト。商用利用時は Yahoo Finance の利用規約に注意し、
公式の有償APIへの差し替えを行ってください。
