// マーケットデータ - ビルド時に Yahoo Finance API を直接フェッチ（Node.js環境）
// 静的サイト生成（SSG）のため CORS は発生しない（サーバ間通信）
// Cloudflare 不使用、プロキシ不要、外部依存最小化

export type MarketCategory =
  | "JP_STOCK"
  | "US_STOCK"
  | "ETF"
  | "FX"
  | "CRYPTO"
  | "COMMODITY_MACRO";

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
  /** 今回の取得に失敗し、前回値（スナップショット）を表示している場合 true */
  stale?: boolean;
  /** この値が取得された時刻（ISO文字列）。前回値フォールバック時は前回取得時刻 */
  asOf?: string;
}

export const WATCHLIST: SymbolDef[] = [
  { symbol: "^N225", displayName: "日経平均株価", category: "JP_STOCK" },
  { symbol: "1306.T", displayName: "TOPIX (ETF連動)", category: "JP_STOCK" },
  { symbol: "7203.T", displayName: "トヨタ自動車", category: "JP_STOCK" },
  { symbol: "6758.T", displayName: "ソニーグループ", category: "JP_STOCK" },
  { symbol: "9984.T", displayName: "ソフトバンクG", category: "JP_STOCK" },
  { symbol: "8306.T", displayName: "三菱UFJ FG", category: "JP_STOCK" },
  { symbol: "8316.T", displayName: "三井住友FG", category: "JP_STOCK" },
  { symbol: "9432.T", displayName: "NTT", category: "JP_STOCK" },
  { symbol: "9433.T", displayName: "KDDI", category: "JP_STOCK" },
  { symbol: "9434.T", displayName: "ソフトバンク", category: "JP_STOCK" },
  { symbol: "6861.T", displayName: "キーエンス", category: "JP_STOCK" },
  { symbol: "9983.T", displayName: "ファーストリテイリング", category: "JP_STOCK" },
  { symbol: "7974.T", displayName: "任天堂", category: "JP_STOCK" },
  { symbol: "8035.T", displayName: "東京エレクトロン", category: "JP_STOCK" },
  { symbol: "6501.T", displayName: "日立製作所", category: "JP_STOCK" },
  { symbol: "8058.T", displayName: "三菱商事", category: "JP_STOCK" },
  { symbol: "4502.T", displayName: "武田薬品", category: "JP_STOCK" },
  { symbol: "6098.T", displayName: "リクルートHD", category: "JP_STOCK" },
  { symbol: "4063.T", displayName: "信越化学工業", category: "JP_STOCK" },
  { symbol: "4568.T", displayName: "第一三共", category: "JP_STOCK" },
  { symbol: "^DJI", displayName: "NYダウ", category: "US_STOCK" },
  { symbol: "^GSPC", displayName: "S&P 500", category: "US_STOCK" },
  { symbol: "^IXIC", displayName: "NASDAQ総合", category: "US_STOCK" },
  { symbol: "^RUT", displayName: "Russell 2000", category: "US_STOCK" },
  { symbol: "AAPL", displayName: "Apple", category: "US_STOCK" },
  { symbol: "MSFT", displayName: "Microsoft", category: "US_STOCK" },
  { symbol: "NVDA", displayName: "NVIDIA", category: "US_STOCK" },
  { symbol: "AMZN", displayName: "Amazon", category: "US_STOCK" },
  { symbol: "GOOGL", displayName: "Alphabet", category: "US_STOCK" },
  { symbol: "META", displayName: "Meta Platforms", category: "US_STOCK" },
  { symbol: "TSLA", displayName: "Tesla", category: "US_STOCK" },
  { symbol: "BRK-B", displayName: "Berkshire Hathaway", category: "US_STOCK" },
  { symbol: "JPM", displayName: "JPMorgan Chase", category: "US_STOCK" },
  { symbol: "V", displayName: "Visa", category: "US_STOCK" },
  { symbol: "MA", displayName: "Mastercard", category: "US_STOCK" },
  { symbol: "LLY", displayName: "Eli Lilly", category: "US_STOCK" },
  { symbol: "AVGO", displayName: "Broadcom", category: "US_STOCK" },
  { symbol: "XOM", displayName: "Exxon Mobil", category: "US_STOCK" },
  { symbol: "COST", displayName: "Costco", category: "US_STOCK" },
  { symbol: "SPY", displayName: "SPDR S&P 500 ETF", category: "ETF" },
  { symbol: "QQQ", displayName: "Invesco QQQ", category: "ETF" },
  { symbol: "VTI", displayName: "Vanguard Total Stock Market", category: "ETF" },
  { symbol: "IWM", displayName: "iShares Russell 2000", category: "ETF" },
  { symbol: "DIA", displayName: "SPDR Dow Jones ETF", category: "ETF" },
  { symbol: "EWJ", displayName: "iShares MSCI Japan", category: "ETF" },
  { symbol: "EFA", displayName: "iShares MSCI EAFE", category: "ETF" },
  { symbol: "EEM", displayName: "iShares MSCI Emerging Markets", category: "ETF" },
  { symbol: "GLD", displayName: "SPDR Gold Shares", category: "ETF" },
  { symbol: "TLT", displayName: "iShares 20+ Year Treasury Bond", category: "ETF" },
  { symbol: "HYG", displayName: "iShares High Yield Bond", category: "ETF" },
  { symbol: "JPY=X", displayName: "USD / JPY", category: "FX" },
  { symbol: "EURJPY=X", displayName: "EUR / JPY", category: "FX" },
  { symbol: "EURUSD=X", displayName: "EUR / USD", category: "FX" },
  { symbol: "GBPJPY=X", displayName: "GBP / JPY", category: "FX" },
  { symbol: "AUDJPY=X", displayName: "AUD / JPY", category: "FX" },
  { symbol: "CHFJPY=X", displayName: "CHF / JPY", category: "FX" },
  { symbol: "CADJPY=X", displayName: "CAD / JPY", category: "FX" },
  { symbol: "NZDJPY=X", displayName: "NZD / JPY", category: "FX" },
  { symbol: "CNYJPY=X", displayName: "CNY / JPY", category: "FX" },
  { symbol: "GBPUSD=X", displayName: "GBP / USD", category: "FX" },
  { symbol: "BTC-USD", displayName: "Bitcoin", category: "CRYPTO" },
  { symbol: "ETH-USD", displayName: "Ethereum", category: "CRYPTO" },
  { symbol: "SOL-USD", displayName: "Solana", category: "CRYPTO" },
  { symbol: "XRP-USD", displayName: "XRP", category: "CRYPTO" },
  { symbol: "BNB-USD", displayName: "BNB", category: "CRYPTO" },
  { symbol: "ADA-USD", displayName: "Cardano", category: "CRYPTO" },
  { symbol: "DOGE-USD", displayName: "Dogecoin", category: "CRYPTO" },
  { symbol: "BTC-JPY", displayName: "Bitcoin (JPY)", category: "CRYPTO" },
  { symbol: "GC=F", displayName: "金先物", category: "COMMODITY_MACRO" },
  { symbol: "SI=F", displayName: "銀先物", category: "COMMODITY_MACRO" },
  { symbol: "CL=F", displayName: "WTI原油先物", category: "COMMODITY_MACRO" },
  { symbol: "NG=F", displayName: "天然ガス先物", category: "COMMODITY_MACRO" },
  { symbol: "HG=F", displayName: "銅先物", category: "COMMODITY_MACRO" },
  { symbol: "^TNX", displayName: "米10年金利", category: "COMMODITY_MACRO" },
  { symbol: "^VIX", displayName: "VIX指数", category: "COMMODITY_MACRO" },
  { symbol: "DX-Y.NYB", displayName: "米ドル指数", category: "COMMODITY_MACRO" },
  { symbol: "^IRX", displayName: "米3カ月短期金利", category: "COMMODITY_MACRO" },
];

const YAHOO_BASE = "https://query1.finance.yahoo.com/v8/finance/chart";
const USER_AGENT = "Mozilla/5.0 (TalmudFinance Web; +https://github.com/)";
const FETCH_BATCH_SIZE = 12;

let quoteCache: Promise<MarketQuote[]> | null = null;

// 前回値スナップショット（ビルド時のフォールバック用）。src/data 配下に置き、Web には配信しない。
const SNAPSHOT_RELATIVE_PATH = "src/data/market-snapshot.json";

interface SnapshotEntry {
  price: number;
  previousClose: number;
  currency: string;
  asOf: string;
}

interface MarketSnapshot {
  updatedAt: string | null;
  quotes: Record<string, SnapshotEntry>;
}

/** 前回値スナップショットを読み込む。存在しない/壊れている場合は空を返す。 */
async function loadSnapshot(): Promise<MarketSnapshot> {
  try {
    const fs = await import("node:fs/promises");
    const path = await import("node:path");
    const filePath = path.resolve(SNAPSHOT_RELATIVE_PATH);
    const text = await fs.readFile(filePath, "utf-8");
    const parsed = JSON.parse(text) as MarketSnapshot;
    if (parsed && typeof parsed === "object" && parsed.quotes) return parsed;
  } catch {
    // 未作成・読み込み失敗時は空スナップショットで続行（ビルドは止めない）
  }
  return { updatedAt: null, quotes: {} };
}

/** 更新後のスナップショットを保存する。失敗してもビルドは止めない。 */
async function saveSnapshot(snapshot: MarketSnapshot): Promise<void> {
  try {
    const fs = await import("node:fs/promises");
    const path = await import("node:path");
    const filePath = path.resolve(SNAPSHOT_RELATIVE_PATH);
    await fs.writeFile(filePath, JSON.stringify(snapshot, null, 2) + "\n", "utf-8");
  } catch (e) {
    console.warn("[market] snapshot save skipped:", (e as Error).message);
  }
}

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
  if (quoteCache) return quoteCache;

  quoteCache = fetchAllUncached();
  return quoteCache;
}

async function fetchAllUncached(): Promise<MarketQuote[]> {
  console.log(`[market] Fetching ${WATCHLIST.length} symbols at build time...`);
  const nowIso = new Date().toISOString();
  const results: (MarketQuote | null)[] = [];
  for (let i = 0; i < WATCHLIST.length; i += FETCH_BATCH_SIZE) {
    const batch = WATCHLIST.slice(i, i + FETCH_BATCH_SIZE);
    results.push(...await Promise.all(batch.map((def) => fetchQuoteWithRetry(def))));
  }

  const freshBySymbol = new Map<string, MarketQuote>();
  for (const q of results) if (q) freshBySymbol.set(q.symbol, q);

  // 前回値スナップショットを読み込み、取得失敗銘柄のフォールバックに使う
  const snapshot = await loadSnapshot();

  const merged: MarketQuote[] = [];
  for (const def of WATCHLIST) {
    const fresh = freshBySymbol.get(def.symbol);
    if (fresh) {
      merged.push({ ...fresh, stale: false, asOf: nowIso });
      // スナップショットを最新値で更新
      snapshot.quotes[def.symbol] = {
        price: fresh.price,
        previousClose: fresh.previousClose,
        currency: fresh.currency,
        asOf: nowIso,
      };
      continue;
    }
    // 取得失敗 → 前回値があればそれを表示（空欄を出さない）
    const prev = snapshot.quotes[def.symbol];
    if (prev) {
      merged.push({
        symbol: def.symbol,
        displayName: def.displayName,
        category: def.category,
        price: prev.price,
        previousClose: prev.previousClose,
        currency: prev.currency,
        stale: true,
        asOf: prev.asOf,
      });
    }
    // 前回値も無ければスキップ（初回かつ取得失敗の銘柄のみ）
  }

  const freshCount = merged.filter((q) => !q.stale).length;
  const staleCount = merged.length - freshCount;
  console.log(
    `[market] ${freshCount} fresh, ${staleCount} from snapshot, ${merged.length}/${WATCHLIST.length} total`,
  );

  // 更新したスナップショットを保存（次回ビルドのフォールバック用）
  snapshot.updatedAt = nowIso;
  await saveSnapshot(snapshot);

  return merged;
}

/** 表示中のデータに前回値フォールバックが含まれるか、最も古い asOf を返す。 */
export function getDataFreshness(quotes: MarketQuote[]): {
  hasStale: boolean;
  oldestAsOf: string | null;
} {
  let hasStale = false;
  let oldest: string | null = null;
  for (const q of quotes) {
    if (q.stale) hasStale = true;
    if (q.asOf && (oldest === null || q.asOf < oldest)) oldest = q.asOf;
  }
  return { hasStale, oldestAsOf: oldest };
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

export interface MarketInsight {
  mood: "リスクオン" | "中立" | "リスクオフ";
  headline: string;
  points: string[];
  focus: string;
  disclaimer: string;
}

function quoteBySymbol(quotes: MarketQuote[], symbol: string): MarketQuote | undefined {
  return quotes.find((q) => q.symbol === symbol);
}

function pct(quotes: MarketQuote[], symbol: string): number | null {
  const q = quoteBySymbol(quotes, symbol);
  return q ? getChange(q).percent : null;
}

function directionText(value: number | null, upWord: string, downWord: string): string {
  if (value === null) return "データ不足";
  if (value > 0.15) return upWord;
  if (value < -0.15) return downWord;
  return "横ばい";
}

function formatPct(value: number | null): string {
  if (value === null) return "取得不可";
  const sign = value > 0 ? "+" : "";
  return `${sign}${value.toFixed(2)}%`;
}

/** ルールベースの市況メモ。投資助言ではなく、値動きの整理に限定する。 */
export function buildMarketInsight(quotes: MarketQuote[]): MarketInsight {
  const sp500 = pct(quotes, "^GSPC");
  const nasdaq = pct(quotes, "^IXIC");
  const nikkei = pct(quotes, "^N225");
  const btc = pct(quotes, "BTC-USD");
  const vix = pct(quotes, "^VIX");
  const gold = pct(quotes, "GC=F");
  const usdJpy = pct(quotes, "JPY=X");
  const tnx = pct(quotes, "^TNX");

  let score = 0;
  for (const value of [sp500, nasdaq, nikkei]) {
    if (value !== null && value > 0.25) score += 1;
    if (value !== null && value < -0.25) score -= 1;
  }
  if (btc !== null && btc > 1) score += 1;
  if (btc !== null && btc < -1) score -= 1;
  if (vix !== null && vix < -2) score += 1;
  if (vix !== null && vix > 2) score -= 1;

  const mood: MarketInsight["mood"] =
    score >= 3 ? "リスクオン" : score <= -3 ? "リスクオフ" : "中立";

  const headline =
    mood === "リスクオン"
      ? "株式や暗号資産を中心に、リスクを取りやすい地合いです。"
      : mood === "リスクオフ"
        ? "株式や暗号資産に慎重さが出やすく、防御姿勢を意識したい地合いです。"
        : "強い一方向ではなく、複数市場を見比べたい中立的な地合いです。";

  const jpyTone = directionText(usdJpy, "円安方向", "円高方向");
  const rateTone = directionText(tnx, "金利上昇方向", "金利低下方向");
  const goldTone = directionText(gold, "金は買われ気味", "金は売られ気味");

  const movers = quotes
    .map((q) => ({ quote: q, change: getChange(q) }))
    .sort((a, b) => Math.abs(b.change.percent) - Math.abs(a.change.percent))
    .slice(0, 3)
    .map(({ quote, change }) => `${quote.displayName}: ${formatPct(change.percent)}`);

  return {
    mood,
    headline,
    points: [
      `株式: S&P 500 ${formatPct(sp500)}、NASDAQ ${formatPct(nasdaq)}、日経平均 ${formatPct(nikkei)}。`,
      `為替: USD/JPY は ${formatPct(usdJpy)} で、足元は ${jpyTone}。`,
      `マクロ: VIX ${formatPct(vix)}、米10年金利 ${formatPct(tnx)}、${rateTone}。${goldTone}。`,
      `変動が大きい指標: ${movers.join(" / ")}`,
    ],
    focus: "今日の値動きだけで結論を急がず、株・為替・金利・商品を並べて確認する局面です。",
    disclaimer: "この市況メモはルールベースの要約であり、売買推奨や投資助言ではありません。",
  };
}
