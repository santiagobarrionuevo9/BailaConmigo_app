import { Injectable } from '@angular/core';
import { DancerProfileResponseDto } from '../models/dancerprofileresponse';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { MatchResponse } from '../models/MatchResponse';

@Injectable({
  providedIn: 'root'
})
export class MatchService {

  private apiUrl = `http://localhost:8080/api/match`;

  constructor(private http: HttpClient) { }

  searchDancers(
    userId: number, 
    city: string, 
    styles: string[], 
    level?: string, 
    availability?: string
  ): Observable<DancerProfileResponseDto[]> {
    let params = new HttpParams()
      .set('userId', userId.toString())
      .set('city', city);
    
    // Añadir cada estilo como un parámetro separado con el mismo nombre
    styles.forEach(style => {
      params = params.append('styles', style);
    });
    
    if (level) {
      params = params.set('level', level);
    }
    
    if (availability) {
      params = params.set('availability', availability);
    }
    
    return this.http.get<DancerProfileResponseDto[]>(`${this.apiUrl}/search`, { params });
  }

    likeProfile(likerId: number, likedProfileId: number): Observable<MatchResponse> {
    if (likerId == null || likedProfileId == null) {
      throw new Error('likerId o likedProfileId no pueden ser null o undefined');
    }

    const params = new HttpParams()
      .set('likerId', likerId.toString())
      .set('likedProfileId', likedProfileId.toString());

    return this.http.post<MatchResponse>(`${this.apiUrl}/like`, null, { params });
  }


  getMatches(userId: number): Observable<DancerProfileResponseDto[]> {
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.get<DancerProfileResponseDto[]>(`${this.apiUrl}/matches`, { params });
  }
}

