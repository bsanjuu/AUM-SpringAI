export interface LoadingStats {
  urlsRequested: number;
  urlsScraped: number;
  chunksCreated: number;
  documentsIndexed: number;
  durationMs: number;
  successRate: string;
  indexingRate: string;
}

export interface LoadingResponse {
  success: boolean;
  message: string;
  stats?: LoadingStats;
  error?: string;
}

export interface IndexingStats {
  success: boolean;
  stats: {
    totalDocuments: number;
    indexedDocuments: number;
    notIndexedDocuments: number;
    indexingRate: number;
    lastUpdate?: string;
    documentsByCategory: { [key: string]: number };
  };
}

export interface DocumentDto {
  id?: number;
  title: string;
  content: string;
  category?: string;
  source?: string;
  metadata?: string;
  createdAt?: string;
  updatedAt?: string;
  indexed?: boolean;
}
