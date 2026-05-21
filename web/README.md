# タルムードに学ぶ — Web/PWA 版

Astro で構築。Android 版と同じコンテンツ（教え100件）を共有しつつ、Web で公開・検証する。

---

## 起動方法（初回 約10分）

### 1. Node.js LTS インストール

https://nodejs.org/ から LTS（推奨）版をダウンロードしてインストール。

確認:

```bash
node -v   # v20.x.x など
npm -v    # 10.x.x など
```

### 2. 依存ライブラリのインストール

このフォルダ内で:

```bash
cd D:\金融市場アプリ用\TalmudFinance\web
npm install
```

約2〜3分。初回は数百MBのダウンロードあり。

### 3. 開発サーバ起動

```bash
npm run dev
```

ブラウザで http://localhost:4321 を開く。

`Ctrl + C` で停止。

---

## 構成

```
web/
├── package.json
├── astro.config.mjs
├── tsconfig.json
├── public/
│   └── favicon.svg            ← 賢者の金の星
└── src/
    ├── lib/
    │   ├── teachings.ts       ← Androidアセット直読・今日の選定ロジック
    │   └── market.ts          ← ビルド時の市場データ取得
    ├── layouts/
    │   └── Layout.astro       ← HTMLシェル + 下部ナビ
    ├── components/
    │   ├── TeachingCard.astro
    │   └── QuoteRow.astro
    ├── pages/
    │   ├── index.astro        ← 今日
    │   ├── market.astro       ← マーケット（4タブ）
    │   └── teachings.astro    ← 教え一覧
    └── styles/
        └── global.css         ← 配色（賢者の金・羊皮紙・インクの黒）
```

---

## 現在の状態

### 動くこと ✅

- 「今日」ページ: 教えカード + 主要4指標サマリー
- 「マーケット」ページ: 4カテゴリのタブ切替（クエリパラメータ）
- 「教え一覧」ページ: 100件のカード表示
- 市場データ: ビルド時に Yahoo Finance から19銘柄を取得
- 教えJSON: `app/src/main/assets/talmud_teachings.json` を1箇所で管理
- レスポンシブ: モバイル優先デザイン
- ダーク/ライトモード: OS 設定に追随

### 未実装（後続フェーズ）

- Phase W4: PWA 化（manifest, Service Worker, ホーム画面追加）
- Phase W5: GitHub Pages 公開
- Phase W6: アナリティクス導入

---

## ビルド

本番ビルド（静的サイト生成）:

```bash
npm run build
```

PowerShell の実行ポリシーで `npm` が止まる場合は、代わりに `npm.cmd run build` を使う。

`dist/` フォルダに静的ファイルが出力される。GitHub Actions ではこの成果物を GitHub Pages にデプロイする。

ローカルプレビュー:

```bash
npm run preview
```

---

## Android 版との関係

| 共有しているもの | 別々に管理しているもの |
|---|---|
| `app/src/main/assets/talmud_teachings.json`（100件のコンテンツ）| UI コード（Kotlin vs Astro）|
| 配色トークン（手動同期）| プラットフォーム固有機能 |
| ロジック式（epochDay % length）| ビルド・配信パイプライン |
| プライバシーポリシー（GitHub Pages 共通）| ストア審査・申請プロセス |

---

## トラブルシューティング

| 症状 | 対処 |
|---|---|
| `npm install` が失敗 | Node.js のバージョンを確認（v18以上）|
| `npm run dev` でポート競合 | 4321 が使用中。別アプリを停止 or `--port 4322` 指定 |
| 教え一覧が空 | `app/src/main/assets/talmud_teachings.json` の存在確認 |
| 文字化け | ターミナルを UTF-8 設定に |
