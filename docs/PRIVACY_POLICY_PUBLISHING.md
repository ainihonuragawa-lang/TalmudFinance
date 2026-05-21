# プライバシーポリシー公開手順

> Google Play 申請には **プライバシーポリシーの公開URL** が必須です。
> 最も安価・確実な方法は GitHub Pages による無料ホスティング。

---

## 前提

- GitHub アカウントがあること（無料アカウントで OK）
- [docs/PRIVACY_POLICY.md](./PRIVACY_POLICY.md) の **【要記入】箇所3つ** を実際の値に置換していること:
  - 提供者（氏名または屋号）
  - 連絡先メールアドレス（2箇所）

---

## 手順A: GitHub Pages で公開（推奨）

### 1. 新規リポジトリを作成

1. GitHub にログイン
2. 右上「+」→「New repository」
3. リポジトリ名: `talmudfinance-privacy`（または好みの名前）
4. Public を選択
5. 「Add a README」にチェック
6. 「Create repository」

### 2. PRIVACY_POLICY.md をアップロード

1. リポジトリ画面で「Add file」→「Upload files」
2. PC から `C:\Projects\TalmudFinance\docs\PRIVACY_POLICY.md` をドラッグ
3. ファイル名を **`index.md`** に変更（重要：GitHub Pages はルートで index.md を探す）
4. 「Commit changes」

### 3. GitHub Pages を有効化

1. リポジトリの **Settings** タブ
2. 左メニュー **Pages**
3. **Source**: 「Deploy from a branch」
4. **Branch**: 「main」、「/(root)」 → **Save**
5. 数分待つと、ページ上部に公開URLが表示される

公開URL例:
```
https://<あなたのGitHubユーザー名>.github.io/talmudfinance-privacy/
```

### 4. URL の動作確認

1. ブラウザで公開URLを開く
2. プライバシーポリシーの内容が表示されること
3. このURL を Google Play Console の申請欄に入力する

---

## 手順B: 既存サイトに掲載

ご自身のブログ、note、個人サイトをお持ちの場合、そこに掲載しても OK です。

要件:
- HTTPS で公開されていること
- 第三者がログインなしで閲覧できること
- 内容が PRIVACY_POLICY.md と完全に一致していること

---

## 手順C: その他のサービス

- **Google Sites**: 無料・簡単。Google アカウントから新規サイト作成し、ページを追加
- **Notion 公開ページ**: Notion ページを「Web で共有」で公開（カスタムドメインは不要）
- **Vercel / Netlify**: 静的サイトホスティング。Markdown を HTML に変換する手間あり

---

## 公開後にやること

### 1. README にリンクを追加（任意）

[README.md](./README.md) の末尾に以下を追記すると見つけやすくなります:

```markdown
## プライバシーポリシー
[プライバシーポリシー](https://...あなたの公開URL...)
```

### 2. Google Play Console に登録

Phase 3 の Play Console 申請時に「**アプリのコンテンツ → プライバシーポリシー**」セクションに公開URLを貼る。

### 3. アプリ内からのリンク（任意）

将来「設定」画面を追加する際に、プライバシーポリシーへのリンクをアプリ内に置くと信頼性が上がる（必須ではない）。

---

## 更新が必要になるタイミング

以下の機能を実装するときは、本ポリシーを **必ず事前に更新** してから機能をリリースする:

- Firebase Crashlytics の統合（#044）
- Firebase Analytics の統合
- プッシュ通知の実装（Phase 4 #050）
- ウォッチリスト編集機能（Phase 4 #052）
- 任意のサーバ通信、データ収集機能

更新の流れ:
1. PRIVACY_POLICY.md を編集
2. 改訂履歴に新バージョン行を追加
3. GitHub リポジトリの index.md を更新（Git で commit & push）
4. GitHub Pages は自動で反映される（数分）
5. 重要な変更ならアプリ内通知またはストア説明欄でアナウンス

---

## チェックリスト

公開作業の完了サイン:

- [ ] PRIVACY_POLICY.md の【要記入】箇所をすべて実値に置換
- [ ] GitHub リポジトリ作成
- [ ] index.md をアップロード
- [ ] GitHub Pages 有効化
- [ ] 公開URLをブラウザで確認
- [ ] URLをメモまたは Play Console に登録準備
