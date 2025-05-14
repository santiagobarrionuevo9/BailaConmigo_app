import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root'
})
export class MercadopagoService {

  private apiUrl ='http://localhost:8080/api';

  constructor(private http: HttpClient) { }

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
}
