import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';
import { AuthService } from '../../../services/auth.service';
import { EventResponseDto } from '../../../models/eventresponse';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
  eventTypes: string[] = []; // Nueva lista para tipos de eventos
  
  // Filtros
  selectedStyle: { id: number; name: string } | null = null;

  selectedLocation: string = '';
  selectedEventType: string = ''; // Nuevo filtro para tipo de evento
  
  // Lista de todos los estilos de baile disponibles
  danceStyles: string[] = [];

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
        this.extractEventTypes(); // Extraer tipos de eventos
      },
      error: (err) => {
        console.error('Error al cargar eventos:', err);
      }
    });
  }

  extractLocations(): void {
    // Extraer ubicaciones únicas para el filtro
    this.locations = [...new Set(this.events.map(event => event.location))].filter(Boolean);
  }

  extractEventTypes(): void {
    // Extraer tipos de eventos únicos para el filtro
    this.eventTypes = [...new Set(this.events.map(event => event.eventType))].filter(Boolean);
  }

  toggleExpand(index: number): void {
    this.expandedEvents[index] = !this.expandedEvents[index];
  }

  resetFilters(): void {
    this.selectedStyle = '' as any;
    this.selectedLocation = '';
    this.selectedEventType = '';
    this.filteredEvents = [...this.events];
    
    // Opcionalmente mostrar mensaje de confirmación
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
        styleMatch = event.danceStyles?.includes(this.selectedStyle) || false;
      }

      
      // Filtrar por tipo de evento si se ha seleccionado uno
      const typeMatch = !this.selectedEventType || event.eventType === this.selectedEventType;
      
      return locationMatch && styleMatch && typeMatch;
    });
  }

  registrarseEvento(eventId: number): void {
    // En un futuro implementará la inscripción al evento
    console.log('Inscripción al evento:', eventId);
    alert('Funcionalidad de inscripción en desarrollo');
  }
}