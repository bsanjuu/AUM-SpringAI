import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { ChatResponse, ChatSession } from '../../models/chat.models';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit {
  private chatService = inject(ChatService);

  userMessage = signal('');
  messages = signal<Array<{ type: 'user' | 'ai'; content: string; confidence?: number; timestamp: Date }>>([]);
  isLoading = signal(false);
  currentCategory = signal<string | undefined>(undefined);
  suggestions = signal<string[]>([]);
  sessionId = signal('');

  ngOnInit() {
    this.chatService.sessionId$.subscribe(id => {
      this.sessionId.set(id);
    });
    
    this.loadSuggestions();
  }

  loadSuggestions() {
    this.chatService.getAllSuggestions().subscribe({
      next: (allSuggestions) => {
        const general = Object.values(allSuggestions).flat().slice(0, 5);
        this.suggestions.set(general);
      },
      error: (err) => console.error('Error loading suggestions:', err)
    });
  }

  sendMessage() {
    const message = this.userMessage().trim();
    if (!message) return;

    this.messages.update(msgs => [...msgs, {
      type: 'user',
      content: message,
      timestamp: new Date()
    }]);

    this.isLoading.set(true);
    this.userMessage.set('');

    this.chatService.sendMessage(message, this.currentCategory()).subscribe({
      next: (response: ChatResponse) => {
        this.messages.update(msgs => [...msgs, {
          type: 'ai',
          content: response.response,
          confidence: response.confidence,
          timestamp: new Date()
        }]);
        
        if (response.suggestions && response.suggestions.length > 0) {
          this.suggestions.set(response.suggestions);
        }
        
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error sending message:', err);
        this.messages.update(msgs => [...msgs, {
          type: 'ai',
          content: 'Sorry, I encountered an error. Please try again.',
          timestamp: new Date()
        }]);
        this.isLoading.set(false);
      }
    });
  }

  selectSuggestion(suggestion: string) {
    this.userMessage.set(suggestion);
    this.sendMessage();
  }

  clearChat() {
    this.messages.set([]);
    this.chatService.clearSession();
  }

  getConfidenceClass(confidence?: number): string {
    if (!confidence) return '';
    if (confidence >= 0.75) return 'high-confidence';
    if (confidence >= 0.5) return 'medium-confidence';
    return 'low-confidence';
  }
}
