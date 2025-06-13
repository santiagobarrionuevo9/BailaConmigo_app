import { Component, OnInit } from '@angular/core';
import { EventRegistrationResponseDto } from '../../../models/EventRegistrationResponseDto';
import { EventRegistrationService } from '../../../services/event-registration.service';
import { UserContextService } from '../../../services/user-context.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-my-registrations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-registrations.component.html',
  styleUrl: './my-registrations.component.css'
})
export class MyRegistrationsComponent implements OnInit {
  registrations: EventRegistrationResponseDto[] = [];
  loading: boolean = false;
  error: string | null = null;
  
  dancerId: number = 0;

  constructor(
    private registrationService: EventRegistrationService, 
    private user: UserContextService
  ) {}

  ngOnInit(): void {
    this.dancerId = this.user.userId || 0;
    if (this.dancerId > 0) {
      this.loadRegistrations();
    } else {
      this.error = 'No se pudo obtener el ID del usuario';
    }
  }

  loadRegistrations(): void {
    this.loading = true;
    this.error = null;
    
    this.registrationService.getRegistrationsByDancer(this.dancerId).subscribe({
      next: (data) => {
        this.registrations = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar las inscripciones', err);
        this.error = 'Error al cargar las inscripciones. Por favor, intenta nuevamente.';
        this.loading = false;
      }
    });
  }

  /**
   * Método para refrescar las inscripciones
   */
  refreshRegistrations(): void {
    this.loadRegistrations();
  }

  /**
   * Método para obtener la clase CSS del badge según el estado
   */
  getStatusBadgeClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'confirmado':
      case 'confirmed':
        return 'badge-confirmado';
      case 'pendiente':
      case 'pending':
        return 'badge-pendiente';
      case 'cancelado':
      case 'cancelled':
        return 'badge-cancelado';
      default:
        return 'badge-pendiente';
    }
  }

  /**
   * Método para formatear la fecha si es necesario
   */
  formatEventDateTime(dateTime: string | Date): string {
    if (!dateTime) return 'Fecha no disponible';
    
    const date = new Date(dateTime);
    return date.toLocaleDateString('es-ES', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Método para determinar si mostrar el botón de pago
   */
  shouldShowPaymentButton(registration: EventRegistrationResponseDto): boolean {
    return !!(registration.paymentUrl && 
             (registration.status?.toLowerCase() === 'pendiente' || 
              registration.status?.toLowerCase() === 'pending'));
  }

  /**
   * Método para manejar errores y mostrar mensajes amigables
   */
  handleError(error: any): string {
    if (error.status === 404) {
      return 'No se encontraron inscripciones';
    } else if (error.status === 500) {
      return 'Error del servidor. Por favor, intenta más tarde';
    } else if (error.status === 0) {
      return 'Error de conexión. Verifica tu conexión a internet';
    }
    return 'Error al cargar las inscripciones';
  }

  /**
   * Método para rastrear elementos en ngFor (mejora el rendimiento)
   */
  trackByRegistrationId(index: number, registration: EventRegistrationResponseDto): any {
    return registration.id || index;
  }
}