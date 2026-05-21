# Android Studio セットアップ完全ガイド (Windows)

> ゼロから始めて TalmudFinance を実機で動かすまでの完全手順。
> 想定所要時間: **1.5〜2時間**（ダウンロード時間含む）
> 必要ディスク容量: **約 15GB**（Android Studio + SDK + プロジェクト）

---

## 0. 始める前のチェック

### 必要スペック（最低）

| 項目 | 必要 | 推奨 |
|---|---|---|
| OS | Windows 10 64bit 以降 | Windows 11 |
| RAM | 8GB | 16GB 以上 |
| 空きディスク | 12GB | 30GB 以上（エミュレータ用） |
| CPU | x86_64 | Intel/AMD 第8世代以降 |
| 画面解像度 | 1280×800 | 1920×1080 以上 |

### 用意するもの

- インターネット接続（合計 5〜10GB ダウンロード）
- 管理者権限のあるユーザーアカウント
- 実機テスト用の Android スマートフォン（オプション・推奨）
- USB ケーブル（実機を使う場合）

---

## 1. Android Studio のダウンロード

### 手順

1. ブラウザで **https://developer.android.com/studio** を開く
2. 「**Download Android Studio Panda 4**」ボタンをクリック
   - ※ 2026年5月時点の最新安定版。表記の数字は時期で変わる
3. 利用規約の **「I have read and agree...」にチェック**
4. 「**Download Android Studio for Windows**」をクリック
5. `android-studio-XXXX.X.X.X-windows.exe` がダウンロードされる（約 1.2GB）

### ダウンロードできないとき

- 大学・職場のネットワーク制限: 自宅 Wi-Fi に切替
- ファイルサイズが小さすぎる: 途中で切れた可能性。再ダウンロード

---

## 2. インストール

### 手順

1. ダウンロードした `.exe` を **ダブルクリック**
2. UAC（ユーザーアカウント制御）で「**はい**」
3. **Welcome to Android Studio Setup** 画面 → 「Next」
4. **Choose Components**:
   - ☑ Android Studio
   - ☑ Android Virtual Device（エミュレータを使うなら）
   - 両方チェックを推奨 → 「Next」
5. **Configuration Settings**: インストール先（既定の `C:\Program Files\Android\Android Studio` のままで OK）→ 「Next」
6. **Choose Start Menu Folder**: 既定のまま「Install」
7. インストール進行（約 5〜10分）→ 完了したら「Next」→「Finish」

### インストールできないとき

- 「**Windows Defender SmartScreen**」が出る: 「詳細情報」→「実行」
- 「**HAXM のインストールに失敗**」: BIOS で仮想化機能 (Intel VT-x / AMD-V) を有効化する必要あり。実機を使うなら無視して OK

---

## 3. 初回起動とセットアップ ウィザード

Android Studio を初めて起動すると **Setup Wizard** が走ります。これが鬼門。

### 手順

1. **Import Android Studio Settings**: 「Do not import settings」→「OK」
2. **データ収集の同意**: お好みで（「Don't send」でも OK）
3. **Welcome 画面** → 「Next」
4. **Install Type**: 「**Standard**」を選択 → 「Next」
   - Custom を選ぶと SDK のバージョン選択ができるが、Standard で問題なし
5. **Select UI Theme**: 「Darcula」（暗）か「Light」（明）を好みで選択 → 「Next」
6. **Verify Settings**: ダウンロード予定のコンポーネント一覧が出る → 「Next」
7. **License Agreement**:
   - 左側のすべての項目をクリック
   - それぞれ右側で「**Accept**」を選択
   - すべて Accept にしたら「Finish」
8. **Downloading Components**:
   - SDK・Build Tools・エミュレータイメージ等を自動ダウンロード
   - 約 3〜5GB、所要 10〜20分
   - 完了したら「Finish」

### セットアップ完了！

「**Welcome to Android Studio**」画面が出れば成功です。

---

## 4. プロジェクトを開く

### 手順

1. Welcome 画面で「**Open**」（または「Open an existing project」）をクリック
2. ファイルブラウザで以下のフォルダを選択:

   ```
   D:\金融市場アプリ用\金融市場アプリ開発\TalmudFinance
   ```

   **重要**: `TalmudFinance` フォルダ自体を選ぶこと（中の `app` や `gradle` は NG）
3. 「OK」をクリック
4. **「Trust Project?」** ダイアログ → 「**Trust Project**」

### プロジェクト開けないとき

- **「This file is not a valid Android Studio project」** → 選んだフォルダ階層が違う。`settings.gradle.kts` がある階層を選ぶ
- **何も起きない** → タスクバーでアイコンが点滅していないか確認。バックグラウンドで読み込んでいる

---

## 5. 初回 Gradle Sync（最重要・最も時間がかかる）

プロジェクトを開くと自動的に **Gradle Sync** が始まります。

### 何が起きているか

- Maven Central / Google Maven から依存ライブラリを自動ダウンロード
- 合計 200〜500MB、初回は **5〜15分**
- 画面下部の「Gradle: ...」プログレスバーで進捗確認

### 「Gradle Wrapper を作成しますか？」が出たら

- 「**Yes**」または「**OK**」を選択
- このプロジェクトには `gradlew` を同梱していないため、初回に Android Studio が作成します

### 終わると

画面下部に「**BUILD SUCCESSFUL**」または「**Gradle sync finished**」が出ます。

### Sync に失敗したら

| 症状 | 対処 |
|---|---|
| 「Could not resolve all files for configuration」 | ネット接続を確認。プロキシ環境ならプロキシ設定 |
| 「Plugin [id: 'com.android.application'] was not found」 | Android Studio バージョン古い。**Help → Check for Updates** |
| 「Unsupported Java」 | **File → Project Structure → SDK Location → Gradle Settings → Gradle JDK** を「**JDK 17**」に変更 |
| 文字化け（日本語パスが原因） | 一時的にプロジェクトを英字パスに移動：例 `C:\Projects\TalmudFinance` |

⚠️ **日本語パス問題**: ご利用のパス `D:\金融市場アプリ用\金融市場アプリ開発\TalmudFinance` は日本語を含みます。Gradle は通常問題なく扱えますが、もしビルドが失敗するなら英数字のみのパスに移動を試してください。

---

## 6. プロジェクトを Make（コンパイル確認）

Gradle Sync が成功したら、実機なしでもコンパイル可否を確認できます。

### 手順

1. メニューバー: **Build → Make Project**（または **Ctrl + F9**）
2. 画面下部の「Build」タブで進捗確認
3. 完了後の表示:
   - ✅ 「**BUILD SUCCESSFUL**」 → 次の章へ
   - ❌ 「**BUILD FAILED**」 → エラーをコピペして Claude に貼り付け

### よくある Make エラー

| エラー | 原因 | 対処 |
|---|---|---|
| `Unresolved reference: ...` | import 漏れ | エラー行で **Alt + Enter** → 「Import」 |
| `Class 'R' is missing` | リソースエラー（先に解消が必要） | 画面上の「Build」タブで赤いエラーを確認 |
| `compileSdk 34 not found` | SDK 未インストール | **Tools → SDK Manager** で「Android 14 (API 34)」をインストール |

---

## 7. 実機 or エミュレータの準備

### A. 実機を使う場合（推奨）

1. Android 端末で **設定 → デバイス情報** を開く
2. 「**ビルド番号**」を **7回連続タップ** → 開発者モード有効化
3. **設定 → システム → 開発者向けオプション** を開く
4. 「**USB デバッグ**」を ON
5. PC と USB ケーブルで接続
6. 端末側に「**USB デバッグを許可しますか?**」 → 「許可」
7. Android Studio 上部のドロップダウンに端末名が出れば成功

#### 端末が認識されないとき

- ケーブルを変える（充電専用ケーブルでは NG。**データ転送対応**のものが必要）
- **設定 → USB の設定** で「ファイル転送 (MTP)」に切替
- USB デバッグを一度 OFF → ON
- メーカー専用ドライバが必要な場合あり（Pixel は不要、その他は要確認）

### B. エミュレータを使う場合

1. **Tools → Device Manager** を開く
2. 「**Create Device**」をクリック
3. **Phone カテゴリ** で「Pixel 7」など好みのモデルを選択 → 「Next」
4. **System Image** で「**API 34 (Android 14)**」を選択
   - 初回は「Download」ボタンを押してダウンロード（約 1GB）
   - ダウンロード後「Next」
5. **AVD Name**: 既定のまま「Finish」
6. 作成された AVD の右側の **▶ ボタン** で起動

#### エミュレータが遅い・起動しない

- BIOS で仮想化機能を有効化（Intel VT-x / AMD-V）
- Hyper-V を OFF（Windows 機能の有効化または無効化）
- 実機を使う方が圧倒的に速い

---

## 8. Run!（実行）

### 手順

1. Android Studio 上部のドロップダウンで **実機 or エミュレータ** を選択
2. 緑色の **▶ Run** ボタン（または **Shift + F10**）をクリック
3. 初回は「**Building**」プログレスが出る（数十秒〜数分）
4. 端末/エミュレータ上にアプリがインストールされ、**自動で起動**

### 期待される画面

「タルムードに学ぶ」というタイトル、今日のタルムード教えカード、主要指標サマリーが表示されればOK。

### Run に失敗したら

| エラー | 対処 |
|---|---|
| `INSTALL_FAILED_USER_RESTRICTED` | 端末で「USB 経由でのアプリインストール」を許可 |
| `DELETE_FAILED_INTERNAL_ERROR` | 既存の同名アプリをアンインストールしてから再 Run |
| `Activity not started` | Manifest と build.gradle の applicationId が一致しているか確認 |

---

## 9. 動作確認（簡易）

実機/エミュレータで以下を確認:

- [ ] アプリが起動する
- [ ] 「今日」タブで教えカードと主要指標が表示
- [ ] 「マーケット」タブで4カテゴリのタブが切替可能
- [ ] 「教え一覧」タブで50件の教えがスクロール表示

すべて OK なら、QA手順書 [QA_AIRPLANE_MODE.md](./QA_AIRPLANE_MODE.md) に進んでください。

---

## 10. 困ったときの情報の出し方

問題が起きたら、以下を Claude に貼り付けてください:

1. **どのステップで止まったか**（このドキュメントのセクション番号）
2. **エラーメッセージの全文**（Android Studio 下部の「Build」タブまたは「Logcat」タブから）
3. **スクリーンショット**（あれば）

例:
```
セクション5の Gradle Sync で失敗。
エラー文:
> Could not resolve com.squareup.retrofit2:retrofit:2.11.0
> Could not GET 'https://repo.maven.apache.org/...'.
```

---

## 11. 参考リンク

- [Android Studio 公式ダウンロード](https://developer.android.com/studio)
- [Android Studio インストール公式手順](https://developer.android.com/studio/install)
- [Gradle Build Tool 公式](https://gradle.org/)
- [Kotlin 公式](https://kotlinlang.org/)

---

## 付録: Android Studio の主要画面の見方

```
┌─────────────────────────────────────────────────┐
│  File  Edit  View  Navigate  Code  Build  Run  Tools  │ ← メニューバー
├─────────────────────────────────────────────────┤
│  [⌂] [Project ▼] [app ▼] [▶ Run] [▼ Device]    │ ← ツールバー
├──────────┬────────────────────────┬─────────────┤
│ Project  │                        │             │
│ tree     │   コードエディタ         │  Gradle/    │
│          │                        │  Device     │
├──────────┴────────────────────────┴─────────────┤
│  Build  Run  Logcat  Terminal  Problems         │ ← 下部タブ
└─────────────────────────────────────────────────┘
```

- **左**: プロジェクトツリー（`app/src/main/java/...` を辿るとソースコード）
- **中央**: 編集中のファイル
- **右**: Gradle / Device Manager
- **下**: ログ・ビルド結果・問題リスト
