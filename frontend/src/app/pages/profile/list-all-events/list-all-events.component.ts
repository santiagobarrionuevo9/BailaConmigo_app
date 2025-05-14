import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';
import { AuthService } from '../../../services/auth.service';
import { EventResponseDto } from '../../../models/eventresponse';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RatingEventDto } from '../../../models/RatingeventDto';

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
  
  // Filtros
  selectedStyle: string | null = null;
  selectedLocation: string = '';
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

  constructor(
    private eventService: EventService,
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

  registrarseEvento(eventId: number): void {
    console.log('Inscripción al evento:', eventId);
    alert('Funcionalidad de inscripción en desarrollo');
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
    
    // Necesario para prevenir problemas de scroll
    document.body.classList.add('modal-open');
  }

  closeRatingModal(): void {
    this.showRatingModal = false;
    this.selectedEventId = null;
    
    // Remover la clase modal-open al cerrar
    document.body.classList.remove('modal-open');
  }

  submitRating(): void {
    if (!this.selectedEventId) return;
    
    // Obtener el ID del usuario actual
    const userId = this.getCurrentUserId();
    
    if (!userId) {
      this.ratingError = 'Debes iniciar sesión para calificar un evento';
      return;
    }

    this.eventService.rateEvent(userId, this.rating).subscribe({
      next: () => {
        this.ratingSuccess = true;
        this.ratingError = '';
        
        // Recargar el evento para obtener la calificación actualizada
        if (this.selectedEventId) {
          this.eventService.getEventById(this.selectedEventId).subscribe({
            next: (updatedEvent) => {
              // Actualizar el evento en la lista
              const index = this.events.findIndex(e => e.id === this.selectedEventId);
              if (index !== -1) {
                this.events[index] = updatedEvent;
                // También actualizar en la lista filtrada si corresponde
                const filteredIndex = this.filteredEvents.findIndex(e => e.id === this.selectedEventId);
                if (filteredIndex !== -1) {
                  this.filteredEvents[filteredIndex] = updatedEvent;
                }
              }
            }
          });
        }
        
        // Cerrar el modal después de un breve tiempo
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