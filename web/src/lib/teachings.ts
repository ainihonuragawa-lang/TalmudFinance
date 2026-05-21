// タルムードの教え - データ型とロジック
// モノリポ構成: Android 版のアセット（app/src/main/assets/talmud_teachings.json）を
// ビルド時に直接読み込む。教えJSON は1箇所のみで管理される。

export interface TalmudTeaching {
  id: number;
  teaching: string;
  source: string;
  lesson: string;
  financial_interpretation: string;
}

export interface TalmudCollection {
  version: number;
  note: string;
  teachings: TalmudTeaching[];
}

/**
 * ビルド時に Android アセットの教えJSONを読み込む。
 * Astro は web/ 配下で実行されるため、相対パスは ../app/...
 */
export async function loadTeachings(): Promise<TalmudCollection> {
  const fs = await import("node:fs/promises");
  const path = await import("node:path");
  const filePath = path.resolve("../app/src/main/assets/talmud_teachings.json");
  const text = await fs.readFile(filePath, "utf-8");
  return JSON.parse(text);
}

/** 日付から決定論的に「今日の教え」を返す（Android版と同じ式） */
export function getTodaysTeaching(
  teachings: TalmudTeaching[],
  date: Date = new Date(),
): TalmudTeaching {
  if (teachings.length === 0) {
    throw new Error("teachings is empty");
  }
  const epochDay = Math.floor(date.getTime() / 86400000);
  const idx = ((epochDay % teachings.length) + teachings.length) %
    teachings.length;
  return teachings[idx]!;
}
