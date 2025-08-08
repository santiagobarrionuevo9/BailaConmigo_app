import { Component, OnInit } from '@angular/core';
import { RegistrationStatus } from '../../../models/RegistrationStatus';
import { EventRegistrationResponseDto } from '../../../models/EventRegistrationResponseDto';
import { EventResponseDto } from '../../../models/eventresponse';
import { EventRegistrationService } from '../../../services/event-registration.service';
import { EventService } from '../../../services/event.service';
import { ActivatedRoute, Router } from '@angular/router';
import { UserContextService } from '../../../services/user-context.service';
import { UpdateAttendanceRequestDto } from '../../../models/update-attendance-request-dto';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-registrastions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './registrations.component.html',
  styleUrl: './registrastions.component.css'
})
export class RegistrastionsComponent implements OnInit {
  registrationStatusEnum = RegistrationStatus;
  registrations: EventRegistrationResponseDto[] = [];
  filteredRegistrations: EventRegistrationResponseDto[] = [];
  event: EventResponseDto | null = null;
  eventId!: number;
  expandedRegistrations: boolean[] = [];
  loading = false;
  attendanceModalVisible = false;
  selectedRegistration: EventRegistrationResponseDto | null = null;
  
  // Propiedad para el buscador
  searchTerm: string = '';
  
  constructor(
    private registrationService: EventRegistrationService,
    private eventService: EventService,
    private route: ActivatedRoute,
    private router: Router,
    private userContext: UserContextService
  ) {}
  
  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('eventId'));
    this.loadEventData();
    this.loadRegistrations();
  }

  loadEventData(): void {
    this.eventService.getEventById(this.eventId).subscribe({
      next: (event) => {
        this.event = event;
      },
      error: (err) => {
        console.error('Error al cargar evento:', err);
        alert('Error al cargar los datos del evento');
      }
    });
  }
  
  loadRegistrations(): void {
    this.loading = true;
    this.registrationService.getRegistrationsByEvent(this.eventId).subscribe({
      next: (registrations) => {
        this.registrations = registrations;
        this.filteredRegistrations = [...registrations]; // Inicializar filteredRegistrations
        this.expandedRegistrations = new Array(this.registrations.length).fill(false);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar inscripciones:', err);
        alert('Error al cargar las inscripciones');
        this.loading = false;
      }
    });
  }
  
  toggleExpand(index: number): void {
    this.expandedRegistrations[index] = !this.expandedRegistrations[index];
  }

  onToggleAttendance(registration: EventRegistrationResponseDto): void {
    if (!this.canToggleAttendance(registration)) {
      return;
    }
    
    this.selectedRegistration = registration;
    this.attendanceModalVisible = true;
  }

  confirmAttendanceToggle(attended: boolean): void {
    if (!this.selectedRegistration) return;

    const dto: UpdateAttendanceRequestDto = { attended };
    
    this.registrationService.updateAttendance(this.selectedRegistration.id, dto).subscribe({
      next: (updatedRegistration) => {
        // Actualizar la inscripción en la lista principal
        const index = this.registrations.findIndex(r => r.id === updatedRegistration.id);
        if (index !== -1) {
          this.registrations[index] = updatedRegistration;
        }
        
        // Actualizar también en la lista filtrada
        const filteredIndex = this.filteredRegistrations.findIndex(r => r.id === updatedRegistration.id);
        if (filteredIndex !== -1) {
          this.filteredRegistrations[filteredIndex] = updatedRegistration;
        }
        
        this.attendanceModalVisible = false;
        this.selectedRegistration = null;
      },
      error: (err) => {
        console.error('Error al actualizar asistencia:', err);
        alert('Error al actualizar la asistencia');
        this.attendanceModalVisible = false;
        this.selectedRegistration = null;
      }
    });
  }

  cancelAttendanceModal(): void {
    this.attendanceModalVisible = false;
    this.selectedRegistration = null;
  }

  goBack(): void {
    this.router.navigate(['/events']);
  }

  getStatusBadgeClass(status: RegistrationStatus): string {
    switch (status) {
      case RegistrationStatus.CONFIRMADO:
        return 'bg-success';
      case RegistrationStatus.CANCELADO:
        return 'bg-danger';
      case RegistrationStatus.PENDIENTE:
        return 'bg-warning';
      default:
        return 'bg-secondary';
    }
  }

  getAttendanceText(attended: boolean | undefined): string {
    if (attended === undefined || attended === null) {
      return 'Sin marcar';
    }
    return attended ? 'Presente' : 'Ausente';
  }

  getAttendanceClass(attended: boolean | undefined): string {
    if (attended === undefined || attended === null) {
      return 'text-secondary';
    }
    return attended ? 'text-success' : 'text-danger';
  }

  // NUEVAS FUNCIONES PARA VALIDACIONES Y BÚSQUEDA

  /**
   * Verifica si el evento ya pasó
   */
  isEventPassed(): boolean {
    if (!this.event?.dateTime) return false;
    const eventDate = new Date(this.event.dateTime);
    const now = new Date();
    return eventDate < now;
  }

  /**
   * Verifica si se puede cambiar la asistencia de una inscripción
   */
  canToggleAttendance(registration: EventRegistrationResponseDto): boolean {
    // No se puede marcar asistencia si el pago está pendiente
    if (registration.status === RegistrationStatus.PENDIENTE) {
      return false;
    }

    // No se puede marcar asistencia si el evento ya pasó
    if (this.isEventPassed()) {
      return false;
    }

    // No se puede marcar asistencia si está cancelado
    if (registration.status === RegistrationStatus.CANCELADO) {
      return false;
    }

    return true;
  }

  /**
   * Obtiene el mensaje de validación para mostrar por qué no se puede marcar asistencia
   */
  getValidationMessage(registration: EventRegistrationResponseDto): string {
    if (registration.status === RegistrationStatus.PENDIENTE) {
      return 'El pago debe estar confirmado para marcar asistencia';
    }

    if (this.isEventPassed()) {
      return 'El evento ya finalizó, no se puede marcar asistencia';
    }

    if (registration.status === RegistrationStatus.CANCELADO) {
      return 'Inscripción cancelada';
    }

    return '';
  }

  /**
   * Obtiene el tooltip para el botón de asistencia
   */
  getAttendanceButtonTooltip(registration: EventRegistrationResponseDto): string {
    if (!this.canToggleAttendance(registration)) {
      return this.getValidationMessage(registration);
    }

    return registration.attended ? 'Marcar como ausente' : 'Marcar como presente';
  }

  /**
   * Función para manejar cambios en el campo de búsqueda
   */
  onSearchChange(): void {
    this.filterRegistrations();
  }

  /**
   * Función para limpiar la búsqueda
   */
  clearSearch(): void {
    this.searchTerm = '';
    this.filterRegistrations();
  }

  /**
   * Función para filtrar las inscripciones según el término de búsqueda
   */
  private filterRegistrations(): void {
    if (!this.searchTerm || this.searchTerm.trim() === '') {
      this.filteredRegistrations = [...this.registrations];
      return;
    }

    const searchLower = this.searchTerm.toLowerCase().trim();
    
    this.filteredRegistrations = this.registrations.filter(registration => {
      // Buscar por nombre del participante
      const nameMatch = registration.dancerName?.toLowerCase().includes(searchLower);
      
      // Buscar por código dinámico
      const codeMatch = registration.codigoDinamico?.toLowerCase().includes(searchLower);
      
      return nameMatch || codeMatch;
    });
  }
}