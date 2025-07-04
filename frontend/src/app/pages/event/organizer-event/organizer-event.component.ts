import { Component, OnInit } from '@angular/core';
import { EventResponseDto } from '../../../models/eventresponse';
import { EventService } from '../../../services/event.service';
import { CommonModule } from '@angular/common';

import { UserContextService } from '../../../services/user-context.service';
import { Router, RouterLink } from '@angular/router';
import { CancelEventRequestDto } from '../../../models/CancelEventRequestDto';
import { EventStatus } from '../../../models/EventStatus';
import { CanceleventComponent } from "../cancelevent/cancelevent.component";

@Component({
  selector: 'app-organizer-event',
  standalone: true,
  imports: [CommonModule, RouterLink, CanceleventComponent],
  templateUrl: './organizer-event.component.html',
  styleUrl: './organizer-event.component.css'
})
export class OrganizerEventComponent implements OnInit {
  eventStatusEnum = EventStatus;
  events: EventResponseDto[] = [];
  organizerId!: number;
  expandedEvents: boolean[] = [];
  cancelPromptVisible = false;
  cancelEventIdToProcess!: number;
  
  constructor(private eventService: EventService,private userContext: UserContextService,private router: Router) {}
  
  ngOnInit(): void {
    this.organizerId = this.userContext.userId!;
    this.eventService.getEventsByOrganizer(this.organizerId).subscribe((res) => {
      this.events = res;
      // Inicializar el array de estados de expansión
      this.expandedEvents = new Array(this.events.length).fill(false);
    });
  }
  
  toggleExpand(index: number): void {
    this.expandedEvents[index] = !this.expandedEvents[index];
  }

  // Ir a la vista de edición (con validación de estado)
  onEditEvent(eventId: number): void {
    const event = this.events.find(e => e.id === eventId);
    if (event && event.status === EventStatus.CANCELADO) {
      alert('No se puede editar un evento cancelado.');
      return;
    }
    this.router.navigate(['/edit-event', eventId]);
  }

  // Ver inscripciones del evento (con validación de estado)
  onViewRegistrations(eventId: number): void {
    const event = this.events.find(e => e.id === eventId);
    if (event && event.status === EventStatus.CANCELADO) {
      alert('No se pueden ver las inscripciones de un evento cancelado.');
      return;
    }
    this.router.navigate(['/registrations', eventId]);
  }

  onCancelEvent(eventId: number): void {
    const event = this.events.find(e => e.id === eventId);
    if (event && event.status === EventStatus.CANCELADO) {
      alert('El evento ya está cancelado.');
      return;
    }
    this.cancelEventIdToProcess = eventId;
    this.cancelPromptVisible = true;
  }

  confirmCancel(reason: string): void {
    const dto: CancelEventRequestDto = { cancellationReason: reason };
    const organizerId = this.userContext.userId!;

    this.eventService.cancelEvent(this.cancelEventIdToProcess, organizerId, dto).subscribe({
      next: () => {
        this.cancelPromptVisible = false;
        this.ngOnInit(); // recargar
      },
      error: err => {
        const message = err?.error?.message || 'Ocurrió un error al cancelar el evento.';
        // Mostrar error personalizado si lo deseas
        alert('Error: ' + message);
        this.cancelPromptVisible = false;
      }
    });
  }
}