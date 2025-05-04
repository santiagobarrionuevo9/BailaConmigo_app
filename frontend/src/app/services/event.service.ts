import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { EventResponseDto } from '../models/eventresponse';
import { CreateEventRequestDto } from '../models/createeventrequest';

@Injectable({
  providedIn: 'root'
})
export class EventService {

  private apiUrl = 'http://localhost:8080/api/events';

  constructor(private http: HttpClient) {}

  getEventsByOrganizer(organizerId: number): Observable<EventResponseDto[]> {
    return this.http.get<EventResponseDto[]>(`${this.apiUrl}/organizer/${organizerId}`);
  }

  createEvent(organizerId: number, event: CreateEventRequestDto): Observable<string> {
    return this.http.post(`${this.apiUrl}/create?organizerId=${organizerId}`, event, {
      responseType: 'text'
    });
  }
  
}
