# Phase W3 / W5: GitHub Pages デプロイ手順

> ビルド時に Yahoo Finance API を直接フェッチ → 静的HTML生成 → GitHub Pages 配信。
> すべて GitHub で完結。

---

## 全体像

```
[Yahoo Finance API]
        ↓ (Node.js fetch, ビルド時のみ)
[GitHub Actions runner]
        ↓ npm run build
[静的HTML/CSS/JS生成]
        ↓ デプロイ
[GitHub Pages 配信]
        ↓
[ユーザー]
```

データ更新サイクル: **30分ごとに自動リビルド**（GitHub Actions cron）。

---

## 1. GitHub リポジトリ作成

### 1.1 新規リポジトリ

ブラウザで https://github.com/new を開き:

- リポジトリ名: `TalmudFinance`
- 可視性: **Public**（無料の GitHub Pages は Public 限定）
- 「Add a README」「.gitignore」「license」: すべて **チェックを外す**（既存ファイルを後から push するため）
- **Create repository**

### 1.2 ローカルプロジェクトを Git 化して push

PowerShell で:

```powershell
cd D:\金融市場アプリ用\TalmudFinance

# 既に Git 管理されているかチェック
git status

# 初回なら:
git init
git add .
git commit -m "Initial commit: TalmudFinance Android + Web monorepo"

# リモート追加 (URL は自分のリポジトリに置き換える)
git remote add origin https://github.com/<あなたのユーザー名>/TalmudFinance.git
git branch -M main
git push -u origin main
```

---

## 2. GitHub Pages の有効化

### 2.1 リポジトリの Settings

GitHub のリポジトリページで:

1. **Settings** タブをクリック
2. 左メニュー **Pages**
3. **Source**: 「**GitHub Actions**」を選択
4. これで GitHub Actions ワークフロー（`.github/workflows/deploy-web.yml`）が自動で使われる

⚠️ Branch ベースの「Deploy from a branch」ではなく **「GitHub Actions」を選ぶ**こと。

---

## 3. ASTRO_SITE と ASTRO_BASE の調整

リポジトリ名が変わる場合でも、現在の workflow は `github.event.repository.name` から `ASTRO_BASE` を自動設定する。

### 方法A: 環境変数で渡す（推奨）

`.github/workflows/deploy-web.yml` の build ステップ:

```yaml
      - name: Build Astro site
        run: npm run build
        env:
          NODE_ENV: production
          ASTRO_SITE: https://<ユーザー名>.github.io
          ASTRO_BASE: /${{ github.event.repository.name }}
```

### 方法B: astro.config.mjs を直接編集

通常は直接編集不要。ローカルでは `/`、GitHub Pages では `/<リポジトリ名>` で動く。

---

## 4. 初回デプロイ

`git push` するか、GitHub の **Actions** タブから **「Build and Deploy to GitHub Pages」→「Run workflow」** で手動実行。

ビルド進捗:

1. Checkout (10秒)
2. Setup Node.js (30秒)
3. npm install (1〜2分)
4. **npm run build**（ここで Yahoo Finance API を19銘柄分フェッチ、1〜2分）
5. Deploy to Pages (30秒)

合計 **約3〜5分** で公開URL が有効になる。

### 公開URL確認

Actions の deploy ジョブ完了後、ジョブ詳細の「**deployment**」ステップに表示される URL を開く:

```
https://<ユーザー名>.github.io/TalmudFinance/
```

---

## 5. 30分ごとの自動リビルド確認

push 後、何もしなくても **30分ごと**に再ビルドが走る:

- GitHub **Actions** タブ → 履歴に「Build and Deploy to GitHub Pages」の自動実行が並ぶ
- 各ビルドで最新の Yahoo Finance データを取得して静的化

cron 表記: `*/30 * * * *` = 毎時0分・30分。

### 注意点

- GitHub Actions の cron は **数分〜十数分遅れる**ことがある（GitHub の負荷次第）
- 厳密なリアルタイム性が必要なら別アーキテクチャを検討（ただし TalmudFinance の用途には十分）

---

## 6. デプロイ確認チェックリスト

公開後:

- [ ] 公開URL を開いて「今日」ページが表示される
- [ ] 教えカードに本日のタルムードが表示
- [ ] 主要4指標（日経平均、S&P500、USD/JPY、Bitcoin）に **実値** が表示
- [ ] マーケットタブで19銘柄すべて表示
- [ ] 教え一覧タブで100件表示
- [ ] スマホで開いて見栄え確認
- [ ] OS ダークモード/ライトモード両方で配色確認

---

## 7. その後の運用

### コード変更時のフロー

```powershell
cd D:\金融市場アプリ用\TalmudFinance
# ... ファイル編集 ...
git add .
git commit -m "変更内容を一言で"
git push
```

push の度に自動ビルド・デプロイされる（cron とは別に即時反映）。

### 教えの追加・修正

`app/src/main/assets/talmud_teachings.json` を編集して push するだけ。
Web 版はビルド時にこの Android アセットを直接読み込む。

---

## 8. トラブルシューティング

| 症状 | 対処 |
|---|---|
| GitHub Actions が失敗 | Actions タブのログを確認。多くは Yahoo Finance のレート制限 → 30分後の cron で自動復旧 |
| 404 で表示されない | Settings → Pages で「GitHub Actions」が選ばれているか確認 |
| CSS が崩れる | `astro.config.mjs` の `base` がリポジトリ名と一致しているか確認 |
| データが古い | Actions タブで最新ビルドの実行時刻確認。手動 Run workflow で即時更新可能 |
| `npm ci` が失敗 | `package-lock.json` を commit に含めているか確認 |
| PowerShell で `npm` が実行できない | `npm.cmd run build` のように `npm.cmd` を使う |

---

## 9. カスタムドメイン（任意・後回し可）

`talmudfinance.com` 等を独自取得したい場合:

1. お名前.com / ムームードメイン等でドメイン取得（約¥1,500/年）
2. GitHub リポジトリ Settings → Pages → Custom domain に入力
3. ドメイン側 DNS 設定:
   - A レコード: 185.199.108.153 / 109 / 110 / 111
   - または CNAME: `<ユーザー名>.github.io`
4. `public/CNAME` ファイル作成（ドメイン名のみ1行）
5. SSL（Let's Encrypt）が自動で発行される（最大24時間）

DNS 設定は GitHub Pages の案内に従う。
