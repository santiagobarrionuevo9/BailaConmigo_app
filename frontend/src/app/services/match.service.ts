import { Injectable } from '@angular/core';
import { DancerProfileResponseDto } from '../models/dancerprofileresponse';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { MatchResponse } from '../models/MatchResponse';
import { UserContextService } from './user-context.service';
import { AuthService } from './auth.service';
import { environment } from '../models/environments';

@Injectable({
  providedIn: 'root'
})
export class MatchService {

  private apiUrl = environment.apiUrl +`/api/match`;
  
  constructor(
    private http: HttpClient,
    private userContext: UserContextService,
    private authService: AuthService
  ) { }

  /**
   * Busca bailarines según el tipo de suscripción del usuario
   * - BASIC: Usa ciudad, nivel y estilos del perfil del usuario automáticamente
   * - PRO: Permite seleccionar parámetros personalizados
   */
  searchDancers(
    userId: number, 
    city?: string, 
    styles?: string[], 
    level?: string, 
    availability?: string
  ): Observable<DancerProfileResponseDto[]> {
    const subscriptionType = this.userContext.SubscriptionType;
    
    if (subscriptionType === 'BASIC') {
      // Para usuarios básicos, usar parámetros del perfil del usuario
      return this.searchDancersBasic(userId);
    } else {
      // Para usuarios PRO, usar parámetros personalizados
      return this.searchDancersPro(userId, city, styles, level, availability);
    }
  }

  /**
   * Búsqueda para usuarios BASIC - usa datos del perfil del usuario
   */
  private searchDancersBasic(userId: number): Observable<DancerProfileResponseDto[]> {
    return new Observable(observer => {
      this.authService.getProfileById(userId).subscribe({
        next: (profile) => {
          let params = new HttpParams()
            .set('userId', userId.toString())
            .set('city', profile.cityName)
            .set('level', profile.level);
          
          // Añadir estilos del perfil del usuario
          if (profile.danceStyles && profile.danceStyles.length > 0) {
            profile.danceStyles.forEach(style => {
              params = params.append('styles', style);
            });
          }
          
          this.http.get<DancerProfileResponseDto[]>(`${this.apiUrl}/search`, { params })
            .subscribe({
              next: (data) => observer.next(data),
              error: (err) => observer.error(err),
              complete: () => observer.complete()
            });
        },
        error: (err) => observer.error(err)
      });
    });
  }

  /**
   * Búsqueda para usuarios PRO - permite parámetros personalizados
   */
  private searchDancersPro(
    userId: number,
    city?: string,
    styles?: string[],
    level?: string,
    availability?: string
  ): Observable<DancerProfileResponseDto[]> {
    let params = new HttpParams().set('userId', userId.toString());
    
    if (city) {
      params = params.set('city', city);
    }
    
    if (styles && styles.length > 0) {
      styles.forEach(style => {
        params = params.append('styles', style);
      });
    }
    
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

