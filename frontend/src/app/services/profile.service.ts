import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { DancerProfileResponseDto } from '../models/dancerprofileresponse';
import { EditDancerProfileDto } from '../models/editdancerprofile';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private baseUrl = 'http://localhost:8080/api/auth'; // Ajustar si cambia

  constructor(private http: HttpClient) {}

  getProfile(id: number): Observable<DancerProfileResponseDto> {
    return this.http.get<DancerProfileResponseDto>(`${this.baseUrl}/${id}`);
  }

  updateProfile(userId: number, dto: EditDancerProfileDto): Observable<any> {
    return this.http.put(`${this.baseUrl}/edit/${userId}`, dto);
  }

  uploadMedia(formData: FormData): Observable<string> {
    return this.http.post('http://localhost:8080/api/media/uploadMedia', formData, { responseType: 'text' });
  }
  
}