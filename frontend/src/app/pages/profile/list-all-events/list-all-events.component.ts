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
import { Country } from '../../../models/Country';
import { LocationService } from '../../../services/location.service';
import Swal from 'sweetalert2';

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
  countries: Country[] = [];
  eventTypes: string[] = [];
  EventStatus = {
    ACTIVO: 'ACTIVO',
    CANCELADO: 'CANCELADO',
    FINALIZADO: 'FINALIZADO'
  };
  
  // Filtros
  selectedStyle: string = '';
  selectedCountry: string = '';
  selectedEventType: string = '';
  
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
    private authService: AuthService,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
    this.loadDanceStyles();
    this.loadCountries();
  }

  loadDanceStyles(): void {
    this.authService.getDanceStyles().subscribe({
      next: (styles) => {
        this.danceStyles = styles;
        console.log('Estilos cargados:', styles);
      },
      error: (err) => {
        console.error('Error al cargar estilos de baile:', err);
      }
    });
  }

  loadCountries(): void {
    this.locationService.getAllCountries().subscribe({
      next: (countries) => {
        this.countries = countries;
      },
      error: (err) => {
        console.error('Error al cargar países:', err);
      }
    });
  }

  loadEvents(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        this.events = data;
        console.log('Eventos cargados:', data);
        this.filteredEvents = [...this.events];
        this.extractEventTypes();
        this.extractDanceStylesFromEvents();
      },
      error: (err) => {
        console.error('Error al cargar eventos:', err);
      }
    });
  }

  extractDanceStylesFromEvents(): void {
    const stylesFromEvents = new Set<string>();
    
    this.events.forEach(event => {
      if (event.danceStyles && Array.isArray(event.danceStyles)) {
        event.danceStyles.forEach(style => {
          if (style && typeof style === 'object' && style.name) {
            stylesFromEvents.add(style.name);
          } else if (typeof style === 'string') {
            stylesFromEvents.add(style);
          }
        });
      }
    });
    
    if (this.danceStyles.length === 0) {
      this.danceStyles = Array.from(stylesFromEvents);
    }
    
    console.log('Estilos extraídos de eventos:', Array.from(stylesFromEvents));
  }

  extractEventTypes(): void {
    this.eventTypes = [...new Set(this.events.map(event => event.eventType))].filter(Boolean);
  }

  toggleExpand(index: number): void {
    this.expandedEvents[index] = !this.expandedEvents[index];
  }

  isPastEvent(event: any): boolean {
    if (!event || !event.dateTime) return false;
    return new Date(event.dateTime) < new Date();
  }

  resetFilters(): void {
    this.selectedStyle = '';
    this.selectedCountry = '';
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
    console.log('Aplicando filtros:', {
      selectedStyle: this.selectedStyle,
      selectedCountry: this.selectedCountry,
      selectedEventType: this.selectedEventType
    });

    this.filteredEvents = this.events.filter(event => {
      const countryMatch = !this.selectedCountry || event.countryName === this.selectedCountry;
      
      let styleMatch = true;
      if (this.selectedStyle) {
        console.log('Filtrando por estilo:', this.selectedStyle);
        console.log('Estilos del evento:', event.danceStyles);
        
        if (event.danceStyles && Array.isArray(event.danceStyles)) {
          styleMatch = event.danceStyles.some(style => {
            if (typeof style === 'object' && style.name) {
              return style.name === this.selectedStyle;
            } else if (typeof style === 'string') {
              return style === this.selectedStyle;
            }
            return false;
          });
        } else {
          styleMatch = false;
        }
      }
      
      const typeMatch = !this.selectedEventType || event.eventType === this.selectedEventType;
      
      console.log(`Evento ${event.name}:`, { countryMatch, styleMatch, typeMatch });
      
      return countryMatch && styleMatch && typeMatch;
    });

    console.log('Eventos filtrados:', this.filteredEvents.length);
  }

  getUniqueCountries(): string[] {
    const countryNames = this.events
      .map(event => event.countryName)
      .filter(Boolean) as string[];
    return [...new Set(countryNames)];
  }

  getEventDanceStyleNames(event: EventResponseDto): string[] {
    if (!event.danceStyles || !Array.isArray(event.danceStyles)) {
      return [];
    }
    
    return event.danceStyles.map(style => {
      if (typeof style === 'object' && style.name) {
        return style.name;
      } else if (typeof style === 'string') {
        return style;
      }
      return '';
    }).filter(name => name !== '');
  }

  // Método principal de inscripción con SweetAlert2
  registrarseEvento(event: EventResponseDto): void {
    const userId = this.getCurrentUserId();
    
    if (!userId) {
      this.registrationError = 'Debes iniciar sesión para inscribirte a un evento';
      this.showRegistrationNotification();
      return;
    }

    const hasPrice = event.price && event.price > 0;

    if (!hasPrice) {
      // Mostrar SweetAlert2 para eventos gratuitos
      this.showFreeEventConfirmation(event);
    } else {
      // Para eventos con pago, proceder directamente
      this.proceedWithRegistration(event);
    }
  }

  // Método para mostrar confirmación de evento gratuito con SweetAlert2
  private showFreeEventConfirmation(event: EventResponseDto): void {
    Swal.fire({
      title: '¡Evento Gratuito!',
      html: `
        <div class="text-center">
          <i class="fas fa-gift fa-3x text-success mb-3"></i>
          <h4>${event.name}</h4>
          <p class="text-muted mb-2">
            <i class="fas fa-calendar me-2"></i>
            ${new Date(event.dateTime).toLocaleDateString('es-ES', {
              day: '2-digit',
              month: '2-digit',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit'
            })}
          </p>
          <p class="text-muted mb-3">
            <i class="fas fa-map-marker-alt me-2"></i>
            ${event.cityName ? event.cityName + ', ' : ''}${event.countryName || ''}
          </p>
          <div class="alert alert-info">
            <i class="fas fa-info-circle me-2"></i>
            Este evento es completamente <strong>GRATUITO</strong>
          </div>
          <p>¿Confirmas tu inscripción?</p>
        </div>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#28a745',
      cancelButtonColor: '#6c757d',
      confirmButtonText: '<i class="fas fa-check me-2"></i>Sí, inscribirme',
      cancelButtonText: '<i class="fas fa-times me-2"></i>Cancelar',
      customClass: {
        popup: 'swal-wide',
        confirmButton: 'btn btn-success',
        cancelButton: 'btn btn-secondary'
      },
      buttonsStyling: false
    }).then((result) => {
      if (result.isConfirmed) {
        this.proceedWithRegistration(event);
      }
    });
  }

  // Método para proceder con la inscripción
  private proceedWithRegistration(event: EventResponseDto): void {
    this.isRegistering = true;
    this.registrationError = '';
    this.registrationSuccess = '';

    const registrationRequest: EventRegistrationRequestDto = {
      eventId: event.id
    };

    const hasPrice = event.price && event.price > 0;

    if (hasPrice) {
      // Evento con pago
      this.eventRegistrationService.registerForEventWithPayment(this.getCurrentUserId()!, registrationRequest).subscribe({
        next: (paymentResponse: PaymentInitiationResponseDto) => {
          this.isRegistering = false;
          this.paymentDetails = paymentResponse;
          this.showPaymentModal = true;
        },
        error: (err) => {
          this.isRegistering = false;
          this.handleRegistrationError(err);
        }
      });
    } else {
      // Evento gratuito
      this.eventRegistrationService.registerForEvent(this.getCurrentUserId()!, registrationRequest).subscribe({
        next: (registrationResponse: EventRegistrationResponseDto) => {
          this.isRegistering = false;
          this.showSuccessAlert(event);
        },
        error: (err) => {
          this.isRegistering = false;
          this.handleRegistrationError(err);
        }
      });
    }
  }

  // Método para mostrar alerta de éxito con SweetAlert2
  private showSuccessAlert(event: EventResponseDto): void {
    Swal.fire({
      title: '¡Inscripción Exitosa!',
      html: `
        <div class="text-center">
          <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
          <h4>Te has inscrito exitosamente en:</h4>
          <h5 class="text-primary">${event.name}</h5>
          <p class="text-muted">
            <i class="fas fa-calendar me-2"></i>
            ${new Date(event.dateTime).toLocaleDateString('es-ES', {
              day: '2-digit',
              month: '2-digit',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit'
            })}
          </p>
          <div class="alert alert-success">
            <i class="fas fa-envelope me-2"></i>
            Recibirás una confirmación por email
          </div>
        </div>
      `,
      icon: 'success',
      confirmButtonColor: '#28a745',
      confirmButtonText: '<i class="fas fa-thumbs-up me-2"></i>¡Genial!',
      customClass: {
        confirmButton: 'btn btn-success'
      },
      buttonsStyling: false
    });
  }

  // Método para manejar errores de inscripción
  private handleRegistrationError(err: any): void {
    const errorMessage = this.getErrorMessage(err);
    
    Swal.fire({
      title: 'Error en la inscripción',
      text: errorMessage,
      icon: 'error',
      confirmButtonColor: '#dc3545',
      confirmButtonText: '<i class="fas fa-times me-2"></i>Entendido',
      customClass: {
        confirmButton: 'btn btn-danger'
      },
      buttonsStyling: false
    });
  }

  // Método para proceder al pago
  proceedToPayment(): void {
    if (this.paymentDetails?.preferenceId) {
      window.location.href = this.paymentDetails.preferenceId;
      this.closePaymentModal();
    }
  }

  closePaymentModal(): void {
    this.showPaymentModal = false;
    this.paymentDetails = null;
  }

  private showRegistrationNotification(): void {
    setTimeout(() => {
      this.registrationError = '';
      this.registrationSuccess = '';
    }, 5000);
  }

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

  canRegister(event: EventResponseDto): boolean {
    const userId = this.getCurrentUserId();
    return userId !== null && event.status === 'ACTIVO';
  }

  isEventAvailable(event: EventResponseDto): boolean {
    return event.status === 'ACTIVO' && (event.availableCapacity ?? 0) > 0;
  }

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

  // Métodos para el modal de calificación
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