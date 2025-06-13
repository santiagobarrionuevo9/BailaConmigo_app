import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { Country } from '../models/Country';
import { catchError } from 'rxjs/internal/operators/catchError';
import { City } from '../models/City';
import { map, throwError } from 'rxjs';
import { environment } from '../models/environments';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private readonly baseUrl = environment.apiUrl +`/api/locations`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene todos los países disponibles
   */
    getAllCountries(): Observable<Country[]> {
    return this.http.get<Country[]>(`${this.baseUrl}/countries`)
      .pipe(catchError(this.handleError));
  }


  /**
   * Obtiene todas las ciudades de un país específico
   */
  getCitiesByCountry(countryId: number): Observable<City[]> {
    return this.http.get<City[]>(`${this.baseUrl}/cities/country/${countryId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Busca ciudades por nombre en todos los países
   */
  searchCities(name: string): Observable<City[]> {
    const params = new HttpParams().set('name', name);
    return this.http.get<City[]>(`${this.baseUrl}/cities/search`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Busca ciudades por nombre dentro de un país específico
   */
  searchCitiesInCountry(countryId: number, name: string): Observable<City[]> {
    const params = new HttpParams().set('name', name);
    return this.http.get<City[]>(`${this.baseUrl}/cities/search/country/${countryId}`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Obtiene un país por su ID
   */
  getCountryById(countryId: number): Observable<Country> {
    return this.getAllCountries().pipe(
      map(countries => {
        const country = countries.find(c => c.id === countryId);
        if (!country) {
          throw new Error(`País con ID ${countryId} no encontrado`);
        }
        return country;
      })
    );
  }

  /**
   * Obtiene una ciudad por su ID
   */
  getCityById(cityId: number): Observable<City> {
    // Nota: Tu API no tiene un endpoint específico para obtener ciudad por ID
    // Por ahora buscaremos en todas las ciudades
    return this.searchCities('').pipe(
      map(cities => {
        const city = cities.find(c => c.id === cityId);
        if (!city) {
          throw new Error(`Ciudad con ID ${cityId} no encontrada`);
        }
        return city;
      })
    );
  }

  private handleError(error: any): Observable<never> {
    console.error('Error en LocationService:', error);
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
