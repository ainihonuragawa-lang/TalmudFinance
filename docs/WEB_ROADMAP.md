# TalmudFinance Web/PWA 版 ロードマップ

> 個人開発の段階戦略: **Web/PWA → Android → iOS**
> Web/PWA を最初に作る理由: 公開コスト・運用コスト・撤退コストが最小、検証が最速。

---

## Phase W0: 現状（再利用可能資産）

Android 版で既に作った資産のうち、Web で活用できるもの:

| 資産 | 状態 | 移植方法 |
|---|---|---|
| `talmud_teachings.json` 100件 | ✅ そのまま | Android アセットをWebビルド時に直読 |
| プライバシーポリシー | ✅ GitHub Pages 公開済み | Web 版URL追加のみ |
| アプリアイコン 512×512 PNG | ✅ そのまま | PWA manifest に流用 |
| フィーチャーグラフィック 1024×500 | ✅ そのまま | OGP 画像に流用 |
| 配色（賢者の金・羊皮紙・インクの黒）| ✅ Color.kt から CSS化 | CSS Variables に変換 |
| Yahoo Finance シンボル19件 | ✅ そのまま | 同じシンボル使用可 |
| 日付ローテーション ロジック | ✅ 概念そのまま | epochDay % length |
| エラーハンドリング戦略 | ✅ パターン継承 | 全失敗→通知、半数失敗→警告 |

---

## Phase W1: スタック選定（このフェーズで決める）

3つのデモコードを実際に見て決めます。**3つとも同じ動作**（今日のタルムードを1件表示）を実装します。

### 共通の最小要件

```
1. talmud_teachings.json をロードする
2. 日付から決定論的に index を計算（Android と同じ式）
3. 教えの本文・出典・解釈を表示する
```

---

### 選択肢A: 素のHTML/JS（最低レイヤー）

**ファイル構成** (1ファイルだけ):

```
talmudfinance-web/
├── index.html      ← これ1つ
└── teachings.json  ← Android からコピー
```

**index.html の全体**:

```html
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>タルムードに学ぶ</title>
  <style>
    :root {
      --sage-gold: #C9A24A;
      --ink-dark: #22201C;
      --parchment: #F6EFD9;
    }
    body {
      background: var(--ink-dark);
      color: var(--parchment);
      font-family: system-ui, -apple-system, sans-serif;
      max-width: 720px;
      margin: 0 auto;
      padding: 20px;
    }
    h1 { color: var(--sage-gold); }
    .card {
      background: var(--sage-gold);
      color: var(--ink-dark);
      padding: 20px;
      border-radius: 8px;
    }
    .teaching { font-size: 1.2em; line-height: 1.6; }
    .source { font-size: 0.9em; opacity: 0.7; margin-top: 8px; }
  </style>
</head>
<body>
  <h1>タルムードに学ぶ</h1>
  <p>賢者の金融市場</p>
  <div class="card" id="card">読み込み中...</div>

  <script>
    fetch('./teachings.json')
      .then(r => r.json())
      .then(data => {
        const all = data.teachings;
        const epochDay = Math.floor(Date.now() / 86400000);
        const idx = ((epochDay % all.length) + all.length) % all.length;
        const t = all[idx];
        document.getElementById('card').innerHTML = `
          <div class="teaching">「${t.teaching}」</div>
          <div class="source">— ${t.source}</div>
          <p><strong>本日の教訓: ${t.lesson}</strong></p>
          <p>${t.financial_interpretation}</p>
        `;
      });
  </script>
</body>
</html>
```

**動かし方**: ブラウザでファイルをダブルクリックするだけ。サーバ不要。

**長所**:
- ビルドツール不要、Node.js 不要
- 全ロジックを1ファイルで把握できる
- デプロイは GitHub Pages にファイル置くだけ

**短所**:
- 規模が大きくなると管理が大変
- TypeScript の型補完が効かない
- PWA 化は手書きで Service Worker

---

### 選択肢B: Astro（コンテンツ重視サイトに最適）

**ファイル構成**:

```
talmudfinance-web/
├── package.json
├── astro.config.mjs
├── public/
│   └── teachings.json
└── src/
    ├── pages/
    │   └── index.astro
    ├── components/
    │   └── TeachingCard.astro
    └── styles/
        └── global.css
```

**src/pages/index.astro**:

```astro
---
import TeachingCard from "../components/TeachingCard.astro";
import "../styles/global.css";

const res = await fetch(new URL("./teachings.json", Astro.url.origin));
const data = await res.json();
const all = data.teachings;
const epochDay = Math.floor(Date.now() / 86400000);
const idx = ((epochDay % all.length) + all.length) % all.length;
const teaching = all[idx];
---

<html lang="ja">
  <head>
    <meta charset="UTF-8" />
    <title>タルムードに学ぶ</title>
  </head>
  <body>
    <h1>タルムードに学ぶ</h1>
    <p>賢者の金融市場</p>
    <TeachingCard teaching={teaching} />
  </body>
</html>
```

**src/components/TeachingCard.astro**:

```astro
---
const { teaching } = Astro.props;
---
<div class="card">
  <div class="teaching">「{teaching.teaching}」</div>
  <div class="source">— {teaching.source}</div>
  <p><strong>本日の教訓: {teaching.lesson}</strong></p>
  <p>{teaching.financial_interpretation}</p>
</div>

<style>
  .card {
    background: var(--sage-gold);
    color: var(--ink-dark);
    padding: 20px;
    border-radius: 8px;
  }
  .teaching { font-size: 1.2em; line-height: 1.6; }
  .source { font-size: 0.9em; opacity: 0.7; margin-top: 8px; }
</style>
```

**初期セットアップ**:

```bash
npm create astro@latest talmudfinance-web -- --template minimal --typescript strict
cd talmudfinance-web
npm install
npm run dev    # http://localhost:4321 で開く
```

**長所**:
- コンポーネント分割でメンテしやすい
- ビルド時に静的HTML生成（SSG）で高速・SEO強い
- React/Vue/Svelte 等もパーシャルに混在可
- Lighthouse スコア90+ 出やすい

**短所**:
- Node.js 必須
- ビルド/デプロイの仕組みを理解する必要あり
- ホットリロードの慣れに数日必要

---

### 選択肢C: Next.js（業界最普及）

**ファイル構成**:

```
talmudfinance-web/
├── package.json
├── next.config.js
├── public/
│   └── teachings.json
└── src/
    └── app/
        ├── page.tsx
        ├── layout.tsx
        └── globals.css
```

**src/app/page.tsx**:

```tsx
import fs from "fs";
import path from "path";

export default function Home() {
  const json = fs.readFileSync(
    path.join(process.cwd(), "public", "teachings.json"),
    "utf-8"
  );
  const data = JSON.parse(json);
  const all = data.teachings;
  const epochDay = Math.floor(Date.now() / 86400000);
  const idx = ((epochDay % all.length) + all.length) % all.length;
  const t = all[idx];

  return (
    <main>
      <h1>タルムードに学ぶ</h1>
      <p>賢者の金融市場</p>
      <div className="card">
        <div className="teaching">「{t.teaching}」</div>
        <div className="source">— {t.source}</div>
        <p><strong>本日の教訓: {t.lesson}</strong></p>
        <p>{t.financial_interpretation}</p>
      </div>
    </main>
  );
}
```

**初期セットアップ**:

```bash
npx create-next-app@latest talmudfinance-web --typescript --app --no-tailwind
cd talmudfinance-web
npm run dev    # http://localhost:3000 で開く
```

**長所**:
- React の知見が世界中で最も豊富
- API Routes でプロキシも同一プロジェクトで書ける
- Vercel デプロイがワンクリック
- 将来の機能拡張に強い

**短所**:
- Astro より初期バンドル大きい
- React の学習曲線が必要（hooks, server components 等）
- 設定オプションが多く迷う

---

## 3スタック 比較表

| 項目 | Vanilla | Astro | Next.js |
|---|---|---|---|
| **学習コスト** | 最小 | 中 | 大 |
| **初期セットアップ** | 0 ステップ | 3 コマンド | 3 コマンド |
| **ファイル数（最小）**| 2 | 6 | 7 |
| **Node.js 必須** | ❌ 不要 | ✅ 必要 | ✅ 必要 |
| **TypeScript** | 別途設定 | デフォルト対応 | デフォルト対応 |
| **コンポーネント分割** | 手動 | ◎ | ◎ |
| **SSG（静的化）** | 元から静的 | ◎ | ◎ |
| **SSR** | ❌ | △ | ◎ |
| **PWA 化** | 手書きSW | プラグイン | プラグイン |
| **Lighthouse** | 90+（簡易）| 95+ | 85+ |
| **デプロイ先**| GitHub Pages | GitHub Pages | Vercel |
| **このプロジェクト適性**| ◯ | ◎ | ○ |

---

## このプロジェクトでの私の推奨

### コンテンツ中心のアプリ → **Astro** が最適

理由:
1. **「教え + 市場データ」はコンテンツ寄り** で SSR ほどの動的性は不要
2. **100件の教えを SSG で全部静的化** すれば SEO で各教えがランディングページになりうる
3. **Lighthouse 95+** が出やすく PWA としての完成度高い
4. **学習コストが Next.js より明らかに低い**
5. **GitHub Pages で静的配信しやすい**

### サブ推奨: Vanilla（速攻で公開したい場合）

「とにかく今日中に公開URLを手にしたい」なら Vanilla。**所要 2〜3時間** で公開可能。

### Next.js を選ぶべき場合

- 将来、ユーザー認証やサーバ DB を含む本格 SaaS にしたい
- React の経験がすでにある
- 投資家向けに見栄えを意識する必要がある

---

## Phase W2 以降（W1 で Astro 採用前提）

### W2: コア実装（6〜10時間）

| タスク | 想定 |
|---|---|
| Astro プロジェクト初期化 | 30分 |
| teachings.json コピー + TypeScript 型定義 | 30分 |
| 配色トークンの CSS Variables 化 | 1時間 |
| ホーム画面（今日の教え + 市場サマリー）| 2時間 |
| マーケット画面（4タブ）| 2時間 |
| 教え一覧画面（100件のスクロール）| 1時間 |
| レスポンシブ対応（モバイル優先）| 1〜2時間 |

### W3: ビルド時マーケットデータ取得（完了）

```
GitHub Actions のビルド時に Yahoo Finance から19銘柄を取得
→ 静的HTMLに反映して GitHub Pages へ配信
```

### W4: PWA 化（1〜2時間）

```
manifest.json + Service Worker（Astro の PWA プラグイン使用）
→ 「ホーム画面に追加」可能、オフラインで教え一覧は閲覧可
```

### W5: 公開（30分〜1時間）

```
GitHub リポジトリ → GitHub Actions → GitHub Pages
→ https://<ユーザー名>.github.io/TalmudFinance/ で公開
```

### W6: 計測（1時間）

```
アクセス解析方式を選定して導入
KPI: DAU, 滞在時間, PWA インストール率, 7日継続率
```

---

## Phase W1 のあなたの次のアクション

1. **この比較表を読む**（10分）
2. **上記3スタックのコード例を眺める**（10分）
3. **直感で1つ選ぶ** または「Astro 推奨で行こう」と判断する
4. **Node.js LTS をインストール**（https://nodejs.org/）
5. **Phase W2 着手の OK を出す**

決まったら知らせてください。Phase W2 のスキャフォルドから着手します。

---

## 補足: Android 版との関係

Web/PWA 版を作っても、**Android ネイティブ版を捨てる必要はない** です:

- Web で検証 → 数字が出ない → Android はリリースしない（コスト保護）
- Web で検証 → 手応えあり → Android リリース → 両方並行展開
- Web で検証 → 機能要求が見える → それを反映して Android 版を改善してからリリース

Web 版と Android 版で **コードベースを分けたまま、コンテンツ（教えJSON）と思想を共有** するのが、個人開発における現実解です。
