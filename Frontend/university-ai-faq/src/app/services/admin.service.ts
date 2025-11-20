import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoadingResponse, IndexingStats, DocumentDto } from '../models/admin.models';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/admin';

  /**
   * Load AUM data from pre-configured URLs
   */
  loadAumData(): Observable<LoadingResponse> {
    return this.http.post<LoadingResponse>(`${this.apiUrl}/load-aum-data`, {});
  }

  /**
   * Load data from custom URLs
   */
  loadFromUrls(urls: string[]): Observable<LoadingResponse> {
    return this.http.post<LoadingResponse>(`${this.apiUrl}/load-from-urls`, urls);
  }

  /**
   * Index a single document
   */
  indexDocument(document: DocumentDto): Observable<LoadingResponse> {
    return this.http.post<LoadingResponse>(`${this.apiUrl}/documents`, document);
  }

  /**
   * Reindex all documents
   */
  reindexAllDocuments(): Observable<LoadingResponse> {
    return this.http.post<LoadingResponse>(`${this.apiUrl}/reindex`, {});
  }

  /**
   * Get indexing statistics
   */
  getIndexingStats(): Observable<IndexingStats> {
    return this.http.get<IndexingStats>(`${this.apiUrl}/stats`);
  }

  /**
   * Health check for admin API
   */
  healthCheck(): Observable<any> {
    return this.http.get(`${this.apiUrl}/health`);
  }
}
