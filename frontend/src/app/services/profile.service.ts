import { HttpClient, HttpHeaders } from '@angular/common/http';
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
    return this.http.get<DancerProfileResponseDto>(`${this.baseUrl}/dancer/${id}`);
  }

  updateProfile(userId: number, dto: EditDancerProfileDto): Observable<string> {
    
    return this.http.put(`${this.baseUrl}/dancer/edit/${userId}`, dto, { responseType: 'text' });
  }

  uploadMedia(formData: FormData): Observable<string> {
    return this.http.post('http://localhost:8080/api/media/uploadMedia', formData, { responseType: 'text' });
  }

  // Método para obtener la imagen con headers de ngrok
  getMediaWithNgrokHeaders(url: string): Observable<Blob> {
    const headers = new HttpHeaders({
      'ngrok-skip-browser-warning': 'true'
    });

    return this.http.get(url, { 
      headers: headers, 
      responseType: 'blob' 
    });
  }

  // Método para convertir la URL problemática en una URL de objeto
  createObjectURL(url: string): Promise<string> {
    return new Promise((resolve, reject) => {
      this.getMediaWithNgrokHeaders(url).subscribe({
        next: (blob) => {
          const objectURL = URL.createObjectURL(blob);
          resolve(objectURL);
        },
        error: (error) => {
          reject(error);
        }
      });
    });
  }

  // Método para limpiar URLs de objeto cuando ya no se necesiten
  revokeObjectURL(url: string): void {
    if (url.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
  }
  
}