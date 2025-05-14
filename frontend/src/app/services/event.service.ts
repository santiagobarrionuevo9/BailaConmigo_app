import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { EventResponseDto } from '../models/eventresponse';
import { CreateEventRequestDto } from '../models/createeventrequest';
import { EditEventRequestDto } from '../models/EditEventRequestDto';
import { CancelEventRequestDto } from '../models/CancelEventRequestDto';
import { RatingEventDto } from '../models/RatingeventDto';

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

  getAllEvents(): Observable<EventResponseDto[]> {
    return this.http.get<EventResponseDto[]>(`${this.apiUrl}/all`);
  }

  editEvent(eventId: number, organizerId: number, dto: EditEventRequestDto): Observable<string> {
    return this.http.put(`${this.apiUrl}/${eventId}/edit?organizerId=${organizerId}`, dto, {
      responseType: 'text'
    });
  }

  cancelEvent(eventId: number, organizerId: number, dto: CancelEventRequestDto): Observable<string> {
    return this.http.put(`${this.apiUrl}/${eventId}/cancel?organizerId=${organizerId}`, dto, {
      responseType: 'text'
    });
  }

  getEventById(eventId: number): Observable<EventResponseDto> {
    return this.http.get<EventResponseDto>(`${this.apiUrl}/${eventId}`);
  }
  
  rateEvent(raterId: number, rating: RatingEventDto): Observable<string> {
    return this.http.post(`${this.apiUrl}/rate?raterId=${raterId}`, rating, {
      responseType: 'text'
    });
  }
}
