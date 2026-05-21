// マーケットデータ - ビルド時に Yahoo Finance API を直接フェッチ（Node.js環境）
// 静的サイト生成（SSG）のため CORS は発生しない（サーバ間通信）
// Cloudflare 不使用、プロキシ不要、外部依存最小化

export type MarketCategory = "JP_STOCK" | "US_STOCK" | "FX" | "CRYPTO";

export interface SymbolDef {
  symbol: string;
  displayName: string;
  category: MarketCategory;
}

export interface MarketQuote {
  symbol: string;
  displayName: string;
  category: MarketCategory;
  price: number;
  previousClose: number;
  currency: string;
}

export const WATCHLIST: SymbolDef[] = [
  { symbol: "^N225", displayName: "日経平均株価", category: "JP_STOCK" },
  { symbol: "1306.T", displayName: "TOPIX (ETF連動)", category: "JP_STOCK" },
  { symbol: "7203.T", displayName: "トヨタ自動車", category: "JP_STOCK" },
  { symbol: "6758.T", displayName: "ソニーグループ", category: "JP_STOCK" },
  { symbol: "9984.T", displayName: "ソフトバンクG", category: "JP_STOCK" },
  { symbol: "^DJI", displayName: "NYダウ", category: "US_STOCK" },
  { symbol: "^GSPC", displayName: "S&P 500", category: "US_STOCK" },
  { symbol: "^IXIC", displayName: "NASDAQ総合", category: "US_STOCK" },
  { symbol: "AAPL", displayName: "Apple", category: "US_STOCK" },
  { symbol: "MSFT", displayName: "Microsoft", category: "US_STOCK" },
  { symbol: "NVDA", displayName: "NVIDIA", category: "US_STOCK" },
  { symbol: "JPY=X", displayName: "USD / JPY", category: "FX" },
  { symbol: "EURJPY=X", displayName: "EUR / JPY", category: "FX" },
  { symbol: "EURUSD=X", displayName: "EUR / USD", category: "FX" },
  { symbol: "GBPJPY=X", displayName: "GBP / JPY", category: "FX" },
  { symbol: "BTC-USD", displayName: "Bitcoin", category: "CRYPTO" },
  { symbol: "ETH-USD", displayName: "Ethereum", category: "CRYPTO" },
  { symbol: "SOL-USD", displayName: "Solana", category: "CRYPTO" },
  { symbol: "BTC-JPY", displayName: "Bitcoin (JPY)", category: "CRYPTO" },
];

const YAHOO_BASE = "https://query1.finance.yahoo.com/v8/finance/chart";
const USER_AGENT = "Mozilla/5.0 (TalmudFinance Web; +https://github.com/)";

/**
 * 単一銘柄を取得（指数バックオフでリトライ）
 * Node.js のグローバル fetch を使用（Astro ビルドは Node 18+）
 */
async function fetchQuoteWithRetry(
  def: SymbolDef,
  maxRetries = 2,
  initialDelayMs = 500,
): Promise<MarketQuote | null> {
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    if (attempt > 0) {
      const delay = initialDelayMs * Math.pow(2, attempt - 1);
      await new Promise((r) => setTimeout(r, delay));
    }
    try {
      const url = `${YAHOO_BASE}/${encodeURIComponent(def.symbol)}?interval=1d&range=5d`;
      const res = await fetch(url, {
        headers: { "User-Agent": USER_AGENT },
        signal: AbortSignal.timeout(8000),
      });
      if (!res.ok) {
        if (res.status >= 400 && res.status < 500 && res.status !== 429) {
          return null; // 4xx はリトライ無意味
        }
        continue; // 5xx, 429 はリトライ
      }
      const json = await res.json();
      const meta = json?.chart?.result?.[0]?.meta;
      const price = meta?.regularMarketPrice;
      if (!meta || typeof price !== "number") return null;
      const prev = meta.previousClose ?? meta.chartPreviousClose ?? price;
      return {
        symbol: def.symbol,
        displayName: def.displayName,
        category: def.category,
        price,
        previousClose: prev,
        currency: meta.currency ?? "",
      };
    } catch (e) {
      // タイムアウト、ネットワーク失敗等はリトライ
      if (attempt === maxRetries) {
        console.warn(`[market] ${def.symbol} fetch failed:`, (e as Error).message);
      }
    }
  }
  return null;
}

/**
 * 全銘柄を並列取得。失敗した銘柄は除外。
 * ビルド時に Node.js プロセスから呼ばれる。
 */
export async function fetchAll(): Promise<MarketQuote[]> {
  console.log(`[market] Fetching ${WATCHLIST.length} symbols at build time...`);
  const results = await Promise.all(
    WATCHLIST.map((def) => fetchQuoteWithRetry(def)),
  );
  const valid = results.filter((q): q is MarketQuote => q !== null);
  console.log(`[market] Got ${valid.length}/${WATCHLIST.length} quotes`);
  return valid;
}

/** カテゴリで絞り込み */
export async function fetchByCategory(
  category: MarketCategory,
): Promise<MarketQuote[]> {
  const all = await fetchAll();
  return all.filter((q) => q.category === category);
}

/** 変化額・変化率 */
export function getChange(q: MarketQuote): {
  change: number;
  percent: number;
  direction: "up" | "down" | "flat";
} {
  const change = q.price - q.previousClose;
  const percent = q.previousClose !== 0 ? (change / q.previousClose) * 100 : 0;
  const direction = change > 0 ? "up" : change < 0 ? "down" : "flat";
  return { change, percent, direction };
}
