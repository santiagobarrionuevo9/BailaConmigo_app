import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MessageDto } from '../models/MessageDto';
import { Observable } from 'rxjs/internal/Observable';
import { ChatMessageDto } from '../models/ChatMessageDto';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private apiUrl = 'http://localhost:8080/api/chat';

  constructor(private http: HttpClient) {}

  sendMessage(message: MessageDto): Observable<any> {
    return this.http.post(`${this.apiUrl}/send`, message);
  }

  getChatHistory(user1Id: number, user2Id: number): Observable<ChatMessageDto[]> {
    const params = new HttpParams()
      .set('user1Id', user1Id)
      .set('user2Id', user2Id);
    return this.http.get<ChatMessageDto[]>(`${this.apiUrl}/history`, { params });
  }
}
