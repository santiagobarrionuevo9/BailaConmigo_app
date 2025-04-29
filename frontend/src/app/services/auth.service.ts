import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { LoginResponseDto } from '../models/login-response';
import { LoginRequestDto } from '../models/login-request';
import { RegisterRequestDto } from '../models/register-request';
import { catchError, map } from 'rxjs';

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
  
}
