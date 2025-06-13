import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { EventRegistrationRequestDto } from '../models/EventRegistrationRequestDto';
import { PaymentInitiationResponseDto } from '../models/PaymentInitiationResponseDto';
import { Observable } from 'rxjs/internal/Observable';
import { EventRegistrationResponseDto } from '../models/EventRegistrationResponseDto';
import { CancelRegistrationRequestDto } from '../models/CancelRegistrationRequestDto';
import { UpdateAttendanceRequestDto } from '../models/update-attendance-request-dto';
import { environment } from '../models/environments';

@Injectable({
  providedIn: 'root'
})
export class EventRegistrationService {
  
  private apiUrl = environment.apiUrl +'/api/event-registrations';

  constructor(private http: HttpClient) {}

  /**
   * Registra un bailarín en un evento con pago
   */
  registerForEventWithPayment(dancerId: number, registrationDto: EventRegistrationRequestDto): Observable<PaymentInitiationResponseDto> {
    const params = new HttpParams().set('dancerId', dancerId.toString());
    return this.http.post<PaymentInitiationResponseDto>(`${this.apiUrl}/register-with-payment`, registrationDto, { params });
  }

  /**
   * Registra un bailarín en un evento gratuito
   */
  registerForEvent(dancerId: number, registrationDto: EventRegistrationRequestDto): Observable<EventRegistrationResponseDto> {
    const params = new HttpParams().set('dancerId', dancerId.toString());
    return this.http.post<EventRegistrationResponseDto>(`${this.apiUrl}/register`, registrationDto, { params });
  }

  /**
   * Cancela una inscripción
   */
  cancelRegistration(dancerId: number, eventId: number, dto: CancelRegistrationRequestDto): Observable<string> {
    const params = new HttpParams().set('dancerId', dancerId.toString());
    return this.http.put(`${this.apiUrl}/${eventId}/cancel`, dto, { 
      params,
      responseType: 'text'
    });
  }

  /**
   * Obtiene las inscripciones de un bailarín
   */
  getRegistrationsByDancer(dancerId: number): Observable<EventRegistrationResponseDto[]> {
    return this.http.get<EventRegistrationResponseDto[]>(`${this.apiUrl}/dancer/${dancerId}`);
  }

  /**
   * Obtiene las inscripciones para un evento
   */
  getRegistrationsByEvent(eventId: number): Observable<EventRegistrationResponseDto[]> {
    return this.http.get<EventRegistrationResponseDto[]>(`${this.apiUrl}/event/${eventId}`);
  }

  /**
   * Confirma el pago de una inscripción
   */
  confirmPayment(registrationId: number, paymentId: string, paymentStatus: string, 
                 paymentMethod?: string, paymentDetails?: string): Observable<string> {
    let params = new HttpParams()
      .set('paymentId', paymentId)
      .set('paymentStatus', paymentStatus);
    
    if (paymentMethod) {
      params = params.set('paymentMethod', paymentMethod);
    }
    
    if (paymentDetails) {
      params = params.set('paymentDetails', paymentDetails);
    }

    return this.http.post(`${this.apiUrl}/confirm-payment/${registrationId}`, null, {
      params,
      responseType: 'text'
    });
  }

  

  /**
   * Actualiza el estado de asistencia de una inscripción
   */
  updateAttendance(registrationId: number, dto: UpdateAttendanceRequestDto): Observable<EventRegistrationResponseDto> {
    return this.http.put<EventRegistrationResponseDto>(`${this.apiUrl}/${registrationId}/attendance`, dto);
  }

}
