import { defineConfig } from "astro/config";

// 公開URL と base パスは GitHub Actions で環境変数として渡す。
// ローカル開発・プレビューでは未設定 → ルート(/) で動く。
// 本番（GitHub Pages）では .github/workflows/deploy-web.yml で
// ASTRO_BASE=/<リポジトリ名> を自動設定する。

const ASTRO_SITE = process.env.ASTRO_SITE || "http://localhost:4321";
const ASTRO_BASE = process.env.ASTRO_BASE || "/";

// https://astro.build/config
export default defineConfig({
  site: ASTRO_SITE,
  base: ASTRO_BASE,
  output: "static",
  build: {
    inlineStylesheets: "auto",
  },
});
