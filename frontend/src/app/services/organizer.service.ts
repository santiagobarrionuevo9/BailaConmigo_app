import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { OrganizerProfileResponseDto } from '../models/OrganizerProfileResponseDto';
import { catchError } from 'rxjs/internal/operators/catchError';
import { EditOrganizerProfileDto } from '../models/EditOrganizerProfileDto';
import { throwError } from 'rxjs/internal/observable/throwError';

@Injectable({
  providedIn: 'root'
})
export class OrganizerService {
  private readonly baseUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene el perfil del organizador por ID de usuario
   */
  getOrganizerProfile(userId: number): Observable<OrganizerProfileResponseDto> {
    return this.http.get<OrganizerProfileResponseDto>(`${this.baseUrl}/organizer/${userId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Actualiza el perfil del organizador
   */
  updateOrganizerProfile(userId: number, dto: EditOrganizerProfileDto): Observable<string> {
    return this.http.put(`${this.baseUrl}/organizer/edit/${userId}`, dto, { responseType: 'text' });
  }
  

  /**
   * Obtiene todos los perfiles de organizadores
   */
  getAllOrganizerProfiles(): Observable<OrganizerProfileResponseDto[]> {
    return this.http.get<OrganizerProfileResponseDto[]>(`${this.baseUrl}/organizer/all`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Sube archivos multimedia (reutilizamos el endpoint del profile service)
   */
  uploadMedia(formData: FormData): Observable<string> {
    return this.http.post('http://localhost:8080/api/media/uploadMedia', formData, { responseType: 'text' });
  }

  private handleError(error: any): Observable<never> {
    console.error('Error en OrganizerService:', error);
    let errorMessage = 'Ha ocurrido un error inesperado';
    
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    } else if (error.status === 0) {
      errorMessage = 'No se puede conectar con el servidor';
    } else if (error.status >= 400 && error.status < 500) {
      errorMessage = 'Error en la solicitud';
    } else if (error.status >= 500) {
      errorMessage = 'Error interno del servidor';
    }

    return throwError(() => new Error(errorMessage));
  }
}
