import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';
import { AuthService } from '../../../services/auth.service';
import { EventResponseDto } from '../../../models/eventresponse';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RatingEventDto } from '../../../models/RatingeventDto';
import { PaymentInitiationResponseDto } from '../../../models/PaymentInitiationResponseDto';
import { EventRegistrationService } from '../../../services/event-registration.service';
import { EventRegistrationRequestDto } from '../../../models/EventRegistrationRequestDto';
import { EventRegistrationResponseDto } from '../../../models/EventRegistrationResponseDto';

@Component({
  selector: 'app-list-all-events',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './list-all-events.component.html',
  styleUrl: './list-all-events.component.css'
})
export class ListAllEventsComponent implements OnInit {
  events: EventResponseDto[] = [];
  filteredEvents: EventResponseDto[] = [];
  expandedEvents: { [key: number]: boolean } = {};
  locations: string[] = [];
  eventTypes: string[] = [];
  EventStatus = {
    ACTIVO: 'ACTIVO',
    CANCELADO: 'CANCELADO',
    FINALIZADO: 'FINALIZADO'
  };
  
  // Filtros
  selectedStyle: string | null = null;
  selectedLocation: string = '';
  selectedEventType: string = '';
  dEvents: EventResponseDto[] = [];
  
  // Lista de todos los estilos de baile disponibles
  danceStyles: string[] = [];

  // Variables para el modal de calificación
  showRatingModal: boolean = false;
  selectedEventId: number | null = null;
  selectedEventName: string = '';
  rating: RatingEventDto = {
    eventId: 0,
    stars: 5,
    comment: ''
  };
  ratingSuccess: boolean = false;
  ratingError: string = '';

  // Variables para inscripción
  isRegistering: boolean = false;
  registrationError: string = '';
  registrationSuccess: string = '';

  // Variables para modal de inscripción con detalles de pago
  showPaymentModal: boolean = false;
  paymentDetails: PaymentInitiationResponseDto | null = null;

  constructor(
    private eventService: EventService,
    private eventRegistrationService: EventRegistrationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
    this.loadDanceStyles();
  }

  loadDanceStyles(): void {
    this.authService.getDanceStyles().subscribe({
      next: (styles) => {
        this.danceStyles = styles;
      },
      error: (err) => {
        console.error('Error al cargar estilos de baile:', err);
      }
    });
  }

  loadEvents(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        this.events = data;
        this.filteredEvents = [...this.events];
        this.extractLocations();
        this.extractEventTypes();
      },
      error: (err) => {
        console.error('Error al cargar eventos:', err);
      }
    });
  }

  extractLocations(): void {
    this.locations = [...new Set(this.events.map(event => event.location))].filter(Boolean);
  }

  extractEventTypes(): void {
    this.eventTypes = [...new Set(this.events.map(event => event.eventType))].filter(Boolean);
  }

  toggleExpand(index: number): void {
    this.expandedEvents[index] = !this.expandedEvents[index];
  }

  // Devuelve true si la fecha del evento ya pasó
  isPastEvent(event: any): boolean {
    if (!event || !event.dateTime) return false;
    return new Date(event.dateTime) < new Date();
  }
  resetFilters(): void {
    this.selectedStyle = null;
    this.selectedLocation = '';
    this.selectedEventType = '';
    this.filteredEvents = [...this.events];
    
    const filterNotification = document.getElementById('filter-notification');
    if (filterNotification) {
      filterNotification.classList.remove('d-none');
      setTimeout(() => {
        filterNotification.classList.add('d-none');
      }, 3000);
    }
  }

  applyFilters(): void {
    this.filteredEvents = this.events.filter(event => {
      // Filtrar por ubicación si se ha seleccionado una
      const locationMatch = !this.selectedLocation || event.location === this.selectedLocation;
      
      // Filtrar por estilo de baile si se ha seleccionado uno
      let styleMatch = true;
      if (this.selectedStyle) {
        styleMatch = event.danceStyles?.some(style => style.name === this.selectedStyle) || false;
      }
      
      // Filtrar por tipo de evento si se ha seleccionado uno
      const typeMatch = !this.selectedEventType || event.eventType === this.selectedEventType;
      
      return locationMatch && styleMatch && typeMatch;
    });
  }

  // Método principal de inscripción
  registrarseEvento(event: EventResponseDto): void {
    const userId = this.getCurrentUserId();
    
    if (!userId) {
      this.registrationError = 'Debes iniciar sesión para inscribirte a un evento';
      this.showRegistrationNotification();
      return;
    }

    this.isRegistering = true;
    this.registrationError = '';
    this.registrationSuccess = '';

    const registrationRequest: EventRegistrationRequestDto = {
      eventId: event.id
    };

    // Verificar si el evento tiene precio
    const hasPrice = event.price && event.price > 0;

    if (hasPrice) {
      // Evento con pago
      this.eventRegistrationService.registerForEventWithPayment(userId, registrationRequest).subscribe({
        next: (paymentResponse: PaymentInitiationResponseDto) => {
          this.isRegistering = false;
          this.paymentDetails = paymentResponse;
          this.showPaymentModal = true;
        },
        error: (err) => {
          this.isRegistering = false;
          this.registrationError = this.getErrorMessage(err);
          this.showRegistrationNotification();
        }
      });
    } 
    else {
      // Evento gratuito
      this.eventRegistrationService.registerForEvent(userId, registrationRequest).subscribe({
        next: (registrationResponse: EventRegistrationResponseDto) => {
          this.isRegistering = false;
          this.registrationSuccess = `¡Te has inscrito exitosamente en ${event.name}!`;
          this.showRegistrationNotification();
        },
        error: (err) => {
          this.isRegistering = false;
          this.registrationError = this.getErrorMessage(err);
          this.showRegistrationNotification();
        }
      });
    }
  }

  // Método para proceder al pago
  proceedToPayment(): void {
    if (this.paymentDetails?.preferenceId) {
      // Abrir URL de pago en una nueva ventana
      window.open(this.paymentDetails.preferenceId, '_blank', 'width=800,height=600');
      this.closePaymentModal();
    }
  }

  // Método para cerrar el modal de pago
  closePaymentModal(): void {
    this.showPaymentModal = false;
    this.paymentDetails = null;
  }

  // Método para mostrar notificaciones de inscripción
  private showRegistrationNotification(): void {
    // Mostrar por 5 segundos
    setTimeout(() => {
      this.registrationError = '';
      this.registrationSuccess = '';
    }, 5000);
  }

  // Método para extraer mensaje de error
  private getErrorMessage(error: any): string {
    if (error.error) {
      if (typeof error.error === 'string') {
        return error.error;
      } else if (error.error.message) {
        return error.error.message;
      }
    }
    return error.message || 'Error al procesar la inscripción';
  }

  // Verificar si el usuario puede inscribirse
  canRegister(event: EventResponseDto): boolean {
    const userId = this.getCurrentUserId();
    return userId !== null && event.status === 'ACTIVO';
  }

  // Verificar si el evento está disponible
  isEventAvailable(event: EventResponseDto): boolean {
    return event.status === 'ACTIVO' && (event.availableCapacity ?? 0) > 0;
  }

  // Obtener texto del botón de inscripción
  getRegistrationButtonText(event: EventResponseDto): string {
    if (!this.canRegister(event)) {
      return 'Iniciar sesión para inscribirse';
    }
    
    if (!this.isEventAvailable(event)) {
      return 'Sin cupo disponible';
    }

    if (this.isRegistering) {
      return 'Procesando...';
    }

    const hasPrice = event.price && event.price > 0;
    return hasPrice ? `Inscribirse ($${event.price})` : 'Inscribirse Gratis';
  }

  // Métodos para el modal de calificación (mantener los existentes)
  openRatingModal(event: EventResponseDto): void {
    this.selectedEventId = event.id;
    this.selectedEventName = event.name;
    this.rating = {
      eventId: event.id,
      stars: 5,
      comment: ''
    };
    this.showRatingModal = true;
    this.ratingSuccess = false;
    this.ratingError = '';
    
    document.body.classList.add('modal-open');
  }

  closeRatingModal(): void {
    this.showRatingModal = false;
    this.selectedEventId = null;
    document.body.classList.remove('modal-open');
  }

  submitRating(): void {
    if (!this.selectedEventId) return;
    
    const userId = this.getCurrentUserId();
    
    if (!userId) {
      this.ratingError = 'Debes iniciar sesión para calificar un evento';
      return;
    }

    this.eventService.rateEvent(userId, this.rating).subscribe({
      next: () => {
        this.ratingSuccess = true;
        this.ratingError = '';
        
        if (this.selectedEventId) {
          this.eventService.getEventById(this.selectedEventId).subscribe({
            next: (updatedEvent) => {
              const index = this.events.findIndex(e => e.id === this.selectedEventId);
              if (index !== -1) {
                this.events[index] = updatedEvent;
                const filteredIndex = this.filteredEvents.findIndex(e => e.id === this.selectedEventId);
                if (filteredIndex !== -1) {
                  this.filteredEvents[filteredIndex] = updatedEvent;
                }
              }
            }
          });
        }
        
        setTimeout(() => {
          this.closeRatingModal();
        }, 2000);
      },
      error: (err) => {
        this.ratingSuccess = false;
        this.ratingError = err.error || 'Error al calificar el evento';
      }
    });
  }

  getCurrentUserId(): number | null {
    const userId = localStorage.getItem('userId');
    return userId ? Number(userId) : null;
  }
}