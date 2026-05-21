# Firebase Crashlytics セットアップ手順

> コード側の準備は完了済み。ここでは Firebase Console での設定と
> `google-services.json` の配置だけ行う。所要時間 **約15分**。

---

## 0. 前提

- Google アカウント（無料）
- アプリのパッケージ名: `com.talmudfinance.app`（変更不可）

すでに [build.gradle.kts](../app/build.gradle.kts) には Crashlytics 関連の設定が入っています:
- ✅ ルート build.gradle.kts: Firebase Gradle プラグイン
- ✅ app/build.gradle.kts: プラグイン適用 + Firebase BoM + Crashlytics 依存
- ✅ AndroidManifest.xml: `firebase_crashlytics_collection_enabled` メタデータ
- ✅ debug ビルドではクラッシュ送信 OFF / release のみ ON

唯一足りないのは **`google-services.json`** ファイルです。

---

## 1. Firebase プロジェクトを作成

### 1.1 Firebase Console を開く

ブラウザで https://console.firebase.google.com/ を開いてログイン。

### 1.2 新規プロジェクト作成

1. **「プロジェクトを追加」** をクリック
2. **プロジェクト名**: `TalmudFinance`（または好きな名前）
3. **「続行」**
4. **Google Analytics**: **無効化** を選択（現段階では不要）
5. **「プロジェクトを作成」**
6. 数十秒待って完了

---

## 2. Android アプリを追加

### 2.1 プラットフォーム選択

プロジェクトのダッシュボードで Android アイコン（緑のドロイドくん）をクリック。

### 2.2 アプリ情報を入力

| 項目 | 入力値 |
|---|---|
| **Android パッケージ名** | `com.talmudfinance.app` ← 厳密に一致が必須 |
| アプリのニックネーム | `TalmudFinance Android`（任意）|
| デバッグ用署名証明書 SHA-1 | 空欄でOK |

「**アプリを登録**」をクリック。

### 2.3 google-services.json をダウンロード

「**google-services.json をダウンロード**」ボタンをクリック。
ダウンロードしたファイルを以下のパスに配置:

```
C:\Projects\TalmudFinance\app\google-services.json
```

⚠️ **`app/` フォルダ直下** に置くこと（`src/main/` ではない）。

### 2.4 残りのステップはスキップ

「Firebase SDK の追加」「アプリで実行して〜」のセクションは **すべてスキップ** して大丈夫です。コード側の修正はすでに完了しています。

「**次へ**」「**コンソールに進む**」を順にクリック。

---

## 3. Crashlytics を有効化

Firebase Console 左メニュー → **「リリースとモニタリング」** → **「Crashlytics」**

1. **「Crashlytics を設定」** をクリック
2. **「アプリを選択」** で TalmudFinance Android を選択
3. **「実行」** で「すでに統合済み」を選ぶ

---

## 4. Android Studio で動作確認

### 4.1 Gradle Sync

`google-services.json` 配置後、Android Studio で:
- 画面上部の **「Sync Now」** バナー → クリック
- なければ **File → Sync Project with Gradle Files**

成功すれば右下に「Gradle sync finished」。

### 4.2 リリースビルドでテスト

Crashlytics は debug ビルドで送信無効なので、release で確認:

```
Build → Generate Signed Bundle / APK
```

または `app/build.gradle.kts` の `debug { ... }` 内の `manifestPlaceholders["crashlyticsCollectionEnabled"] = false` を一時的に `true` に変えて debug で送信させる（テスト後は戻す）。

### 4.3 テストクラッシュを発生させる

検証したい場合、`MainActivity.kt` の `onCreate` の最後に一時的に追加:

```kotlin
// テスト用: 検証後、必ず削除する
throw RuntimeException("Test Crash")
```

アプリ起動 → 即クラッシュ → 再起動して Firebase Console を確認。
**5〜10分以内に Crashlytics ダッシュボードに「Test Crash」が表示**されれば成功。

⚠️ **テスト後は必ずこの行を削除する**こと。

---

## 5. プライバシーポリシーの更新（必須）

Crashlytics が **動作開始した時点で**、収集情報を反映するため、
[PRIVACY_POLICY.md](./PRIVACY_POLICY.md) を以下のように更新:

### 5.1 第10章から Crashlytics の行を削除

```markdown
| クラッシュレポート収集（Firebase Crashlytics）| ... |  ← この行を削除
```

### 5.2 新章「3.2 Firebase Crashlytics」を追加

```markdown
### 3.2 Firebase Crashlytics

- **用途**: クラッシュ・予期せぬエラーを自動検知し、改善に役立てる
- **収集される情報**:
  - クラッシュ発生時のスタックトレース
  - 端末モデル、Android バージョン、アプリバージョン
  - 匿名のインストールID（Firebase 自動生成、個人特定不可）
- **収集されない情報**: 氏名、メールアドレス、位置情報、利用者特定情報
- **送信タイミング**: クラッシュ発生時、次回起動時に Firebase へ送信
- **送信先**: Google LLC（Firebase）
- **保存期間**: Firebase 規約に従う（通常90日）
- **デバッグビルドでは収集無効化**済み
```

### 5.3 改訂履歴に行追加

```markdown
| 1.1 | 2026-MM-DD | Firebase Crashlytics 統合に伴う追記 |
```

### 5.4 GitHub Pages の index.md を更新

ローカル編集後、GitHub リポジトリの `index.md` も同じ内容に更新してコミット。数分で公開URLに反映。

---

## 6. データセーフティ宣言（Play Console）の準備

Phase 3 の Play Console 申請時に以下を宣言:

| 質問 | 回答 |
|---|---|
| クラッシュログの収集 | はい（Firebase Crashlytics 経由）|
| 端末またはその他の識別子 | はい（Firebase インストール ID、サービス改善のみ）|
| 個人情報の収集 | いいえ |
| データの暗号化 | はい（HTTPS）|
| データ削除要求への対応 | はい（連絡先メール経由）|

---

## トラブルシューティング

| 症状 | 対処 |
|---|---|
| Gradle Sync 失敗：`google-services.json not found` | `app/google-services.json` に配置されているか確認 |
| `Plugin [id: 'com.google.gms.google-services'] was not found` | ルート build.gradle.kts のプラグイン行を確認 |
| `Package name does not match` | Firebase Console 登録パッケージ名と一致しているか |
| ダッシュボードにクラッシュ表示なし | release ビルドで起動したか確認 |
| クラッシュ送信に時間がかかる | 通常5〜10分、最大1時間 |

---

## チェックリスト

- [ ] Firebase プロジェクト作成
- [ ] Android アプリ登録（`com.talmudfinance.app`）
- [ ] `google-services.json` を `app/` に配置
- [ ] Android Studio で Gradle Sync 成功
- [ ] release ビルドでテストクラッシュ送信成功
- [ ] Firebase Console にクラッシュ表示確認
- [ ] テストクラッシュコード削除
- [ ] プライバシーポリシー v1.1 更新・GitHub Pages 再公開
- [ ] BACKLOG #044 を ✅ に更新
