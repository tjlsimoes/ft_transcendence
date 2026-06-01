import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChatMessage } from '../../shared/models/chat.model';
import { WebSocketService } from './websocket.service';
import { Page } from '../../shared/models/page.model';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private wsService = inject(WebSocketService);
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/chat`;

  getHistory(recipientId: number): Observable<Page<ChatMessage>> {
    return this.http.get<Page<ChatMessage>>(`${this.baseUrl}/${recipientId}?page=0&size=30`);
  }

  sendMessage(recipientId: number, content: string): void {
    this.wsService.publish('/app/chat', { recipientId, content });
  }

  subscribeToConversation(myId: number, recipientId: number): Observable<ChatMessage> {
    const chatId = [myId, recipientId].sort((a, b) => a - b).join('-');
    return this.wsService.subscribe<ChatMessage>(`/topic/chat/${chatId}`);
  }
}
