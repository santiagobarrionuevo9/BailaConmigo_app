import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MessageDto } from '../models/MessageDto';
import { Observable } from 'rxjs/internal/Observable';
import { ChatMessageDto } from '../models/ChatMessageDto';
import { environment } from '../models/environments';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private apiUrl = environment.apiUrl +'/api/chat';

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
