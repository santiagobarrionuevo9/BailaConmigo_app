import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MessageDto } from '../models/MessageDto';
import { Observable } from 'rxjs/internal/Observable';
import { ChatMessageDto } from '../models/ChatMessageDto';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private apiUrl = 'https://0f78-152-171-81-105.ngrok-free.app/api/chat';

  constructor(private http: HttpClient) {}

  sendMessage(messageDto: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/send`, messageDto);
  }

  getChatHistory(user1Id: number, user2Id: number): Observable<ChatMessageDto[]> {
    return this.http.get<ChatMessageDto[]>(`${this.apiUrl}/history`, {
      params: {
        user1Id: user1Id.toString(),
        user2Id: user2Id.toString()
      }
    });
  }
}
