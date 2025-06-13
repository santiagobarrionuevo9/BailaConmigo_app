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

@Component({
  selector: 'app-registrastions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './registrastions.component.html',
  styleUrl: './registrastions.component.css'
})
export class RegistrastionsComponent implements OnInit {
  registrationStatusEnum = RegistrationStatus;
  registrations: EventRegistrationResponseDto[] = [];
  event: EventResponseDto | null = null;
  eventId!: number;
  expandedRegistrations: boolean[] = [];
  loading = false;
  attendanceModalVisible = false;
  selectedRegistration: EventRegistrationResponseDto | null = null;
  
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
    this.selectedRegistration = registration;
    this.attendanceModalVisible = true;
  }

  confirmAttendanceToggle(attended: boolean): void {
    if (!this.selectedRegistration) return;

    const dto: UpdateAttendanceRequestDto = { attended };
    
    this.registrationService.updateAttendance(this.selectedRegistration.id, dto).subscribe({
      next: (updatedRegistration) => {
        // Actualizar la inscripción en la lista
        const index = this.registrations.findIndex(r => r.id === updatedRegistration.id);
        if (index !== -1) {
          this.registrations[index] = updatedRegistration;
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
}