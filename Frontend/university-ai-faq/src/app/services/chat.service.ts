import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { ChatRequest, ChatResponse, ChatSession, FeedbackRequest } from '../models/chat.models';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/chat';
  
  private sessionIdSubject = new BehaviorSubject<string>(this.generateSessionId());
  sessionId$ = this.sessionIdSubject.asObservable();

  sendMessage(message: string, category?: string): Observable<ChatResponse> {
    const request: ChatRequest = {
      message,
      sessionId: this.sessionIdSubject.value,
      category
    };
    
    return this.http.post<ChatResponse>(this.apiUrl, request);
  }

  getChatHistory(sessionId: string, limit: number = 50): Observable<ChatSession[]> {
    return this.http.get<ChatSession[]>(`${this.apiUrl}/history/${sessionId}?limit=${limit}`);
  }

  getSuggestions(category: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/suggestions/${category}`);
  }

  getAllSuggestions(): Observable<{ [key: string]: string[] }> {
    return this.http.get<{ [key: string]: string[] }>(`${this.apiUrl}/suggestions`);
  }

  submitFeedback(feedback: FeedbackRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/feedback`, feedback);
  }

  clearSession(): void {
    this.sessionIdSubject.next(this.generateSessionId());
  }

  private generateSessionId(): string {
    const timestamp = new Date().getTime();
    const random = Math.random().toString(36).substr(2, 9);
    return 'session-' + timestamp + '-' + random;
  }

  getCurrentSessionId(): string {
    return this.sessionIdSubject.value;
  }
}
