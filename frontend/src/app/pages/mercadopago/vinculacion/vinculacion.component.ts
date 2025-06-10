import { Component, OnInit } from '@angular/core';
import { MercadopagoService } from '../../../services/mercadopago.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserContextService } from '../../../services/user-context.service';

@Component({
  selector: 'app-vinculacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vinculacion.component.html',
  styleUrl: './vinculacion.component.css'
})
export class VinculacionComponent implements OnInit {
  isConnecting = false;
  error: string | null = null;
  userInfo: any = null;

  constructor(
    private mercadoPagoService: MercadopagoService,
    public userContext: UserContextService
  ) {}

  ngOnInit(): void {
    // Cargar información del usuario
    this.loadUserInfo();
    
    // Verificar autenticación
    if (!this.hasValidAuth) {
      this.error = 'Sesión no válida. Por favor, inicia sesión nuevamente.';
    }
  }

  get hasValidAuth(): boolean {
    return this.mercadoPagoService.hasValidToken() && !!this.userContext.userId;
  }

  get canConnect(): boolean {
    return !!this.userContext.userId;
  }

  loadUserInfo(): void {
    this.userInfo = {
      fullName: this.userContext.fullName,
      email: this.userContext.Email,
      userId: this.userContext.userId,
      role: this.userContext.role
    };
  }

  connectToMercadoPago(): void {
    const userId = this.userContext.userId;
    
    if (!userId) {
      this.error = 'No se pudo obtener el ID de usuario';
      return;
    }

    if (!this.hasValidAuth) {
      this.error = 'No tienes autorización para realizar esta acción';
      return;
    }

    this.isConnecting = true;
    this.error = null;

    try {
      this.mercadoPagoService.redirectToMercadoPago(userId);
      // El usuario será redirigido a MercadoPago
    } catch (error: any) {
      this.isConnecting = false;
      
      if (error.message === 'No hay token de autenticación disponible') {
        this.error = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
      } else {
        this.error = 'Error al conectar con MercadoPago. Inténtalo de nuevo.';
      }
      
      console.error('Error:', error);
    }
  }

  redirectToLogin(): void {
    this.userContext.clear();
    // Aquí deberías redirigir a tu página de login
    // window.location.href = '/login';
    // o usar el Router si prefieres
  }
}