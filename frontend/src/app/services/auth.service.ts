import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { LoginResponseDto } from '../models/login-response';
import { LoginRequestDto } from '../models/login-request';
import { DancerProfileResponseDto } from '../models/dancerprofileresponse';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/auth'; // Ajustá la URL real

  constructor(private http: HttpClient) {}

  login(request: LoginRequestDto): Observable<LoginResponseDto> {
    return this.http.post<LoginResponseDto>(`${this.apiUrl}/login`, request);
  }

  register(registerRequest: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, registerRequest, { responseType: 'text' });
  }
  
  /**
   * Solicita un correo de recuperación de contraseña
   * @param request Objeto con el email del usuario
   */
  forgotPassword(request: { email: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, request, { responseType: 'text' });
  }
  
  /**
   * Restablece la contraseña del usuario usando un token
   * @param request Objeto con el token y la nueva contraseña
   */
  resetPassword(request: { token: string, newPassword: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, request, { responseType: 'text' });
  }

  getDanceStyles(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/dance-styles`);
  }

  getProfileById(userId: number): Observable<DancerProfileResponseDto> {
  return this.http.get<DancerProfileResponseDto>(`${this.apiUrl}/dancer/${userId}`);
  }

  // src/app/services/auth.service.ts
  rateProfile(raterId: number, rating: { profileId: number, stars: number, comment: string }): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/rate?raterId=${raterId}`, rating);
  }
  
  
}