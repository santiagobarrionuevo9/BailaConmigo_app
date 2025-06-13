import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { UserContextService } from './user-context.service';

@Injectable({
  providedIn: 'root'
})
export class MercadopagoService {

  private apiUrl ='https://0f78-152-171-81-105.ngrok-free.app/api';

  constructor(private http: HttpClient,private userContext: UserContextService) { }

  /**
   * Genera un link de pago para la suscripción PRO
   * @param email Email del usuario
   * @returns Un Observable con el link de pago
   */
  generateProSubscriptionLink(email: string): Observable<{ init_point: string }> {
    return this.http.get<{ init_point: string }>(`${this.apiUrl}/mercadopago/generar-pago-pro?email=${email}`);
  }

  /**
   * Guarda la información de suscripción en el localStorage
   * @param type Tipo de suscripción (BASICO, PRO)
   * @param expirationDate Fecha de expiración (si es PRO)
   */
  saveSubscriptionInfo(type: string, expirationDate?: string): void {
    localStorage.setItem('subscriptionType', type);
    if (expirationDate) {
      localStorage.setItem('subscriptionExpiration', expirationDate);
    } else {
      localStorage.removeItem('subscriptionExpiration');
    }
  }

  /**
   * Obtiene el tipo de suscripción actual
   * @returns Tipo de suscripción (BASICO, PRO)
   */
  getSubscriptionType(): string {
    return localStorage.getItem('subscriptionType') || 'BASICO';
  }

  /**
   * Obtiene la fecha de expiración de la suscripción
   * @returns Fecha de expiración en formato string o null si no hay
   */
  getSubscriptionExpiration(): string | null {
    return localStorage.getItem('subscriptionExpiration');
  }

  /**
   * Verifica si la suscripción está activa
   * @returns true si es PRO y no ha expirado, false en caso contrario
   */
  isProSubscriptionActive(): boolean {
    const type = this.getSubscriptionType();
    if (type !== 'PRO') return false;

    const expiration = this.getSubscriptionExpiration();
    if (!expiration) return false;

    const expirationDate = new Date(expiration);
    const today = new Date();
    
    return expirationDate > today;
  }

  /**
   * Limpia los datos de suscripción del localStorage
   */
  clearSubscriptionData(): void {
    localStorage.removeItem('subscriptionType');
    localStorage.removeItem('subscriptionExpiration');
  }
  /**
   * Crea los headers con el token de autenticación
   */
  private createAuthHeaders(): HttpHeaders {
    const token = this.userContext.token;
    
    if (!token) {
      throw new Error('No hay token de autenticación disponible');
    }

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

   /**
   * Genera la URL de autorización de MercadoPago para un usuario
   */
  generateAuthUrl(userId: number): Observable<{authUrl: string}> {
    const headers = this.createAuthHeaders();
    
    return this.http.post<{authUrl: string}>(
      `${this.apiUrl}/mercadopago/connect/${userId}`, 
      {}, // body vacío
      { headers }
    );
  }

  /**
   * Redirige al usuario a MercadoPago para autorización
   */
  redirectToMercadoPago(userId: number): void {
    this.generateAuthUrl(userId).subscribe({
      next: (response) => {
        // Abrir en la misma ventana
        window.location.href = response.authUrl;
      },
      error: (error) => {
        console.error('Error al generar URL de autorización:', error);
        throw error;
      }
    });
  }

  /**
   * Verifica si el usuario tiene token de autenticación
   */
  hasValidToken(): boolean {
    return !!this.userContext.token;
  }
}
