import { Component, OnInit } from '@angular/core';
import { EventResponseDto } from '../../../models/eventresponse';
import { EventService } from '../../../services/event.service';
import { CommonModule } from '@angular/common';

import { UserContextService } from '../../../services/user-context.service';
import { Router, RouterLink } from '@angular/router';
import { CancelEventRequestDto } from '../../../models/CancelEventRequestDto';
import { EventStatus } from '../../../models/EventStatus';

@Component({
  selector: 'app-organizer-event',
  standalone: true,
  imports: [  CommonModule, RouterLink],
  templateUrl: './organizer-event.component.html',
  styleUrl: './organizer-event.component.css'
})
export class OrganizerEventComponent implements OnInit {
  eventStatusEnum = EventStatus;
  events: EventResponseDto[] = [];
  organizerId!: number;
  expandedEvents: boolean[] = [];
  
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

  // Ir a la vista de edición (debes tener una ruta tipo /edit-event/:id)
  onEditEvent(eventId: number): void {
    this.router.navigate(['/edit-event', eventId]);
  }

  // Cancelar evento
  onCancelEvent(eventId: number): void {
    const reason = prompt('Motivo de la cancelación:');
    if (!reason) return;

    const organizerId = this.userContext.userId!;
    const dto: CancelEventRequestDto = { cancellationReason: reason };

    this.eventService.cancelEvent(eventId, organizerId, dto).subscribe({
      next: () => {
        alert('Evento cancelado correctamente.');
        // Recargar eventos
        this.ngOnInit();
      },
      error: err => {
      const message = err?.error?.message || err?.message || 'Ocurrió un error al cancelar el evento.';
      alert('Error al cancelar el evento: ' + message);
    }

    });
  }
}
