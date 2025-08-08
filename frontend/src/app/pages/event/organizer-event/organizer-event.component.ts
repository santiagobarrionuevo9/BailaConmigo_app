import { Component, OnInit } from '@angular/core';
import { EventResponseDto } from '../../../models/eventresponse';
import { EventService } from '../../../services/event.service';
import { CommonModule } from '@angular/common';

import { UserContextService } from '../../../services/user-context.service';
import { Router, RouterLink } from '@angular/router';
import { CancelEventRequestDto } from '../../../models/CancelEventRequestDto';
import { EventStatus } from '../../../models/EventStatus';
import { CanceleventComponent } from "../cancelevent/cancelevent.component";
import Swal from 'sweetalert2';

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
  
  constructor(
    private eventService: EventService,
    private userContext: UserContextService,
    private router: Router
  ) {}
  
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
      Swal.fire({
        title: 'Acción no permitida',
        text: 'No se puede editar un evento cancelado.',
        icon: 'warning',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#cc5500'
      });
      return;
    }
    this.router.navigate(['/edit-event', eventId]);
  }

  // Ver inscripciones del evento (con validación de estado)
  onViewRegistrations(eventId: number): void {
    const event = this.events.find(e => e.id === eventId);
    if (event && event.status === EventStatus.CANCELADO) {
      Swal.fire({
        title: 'Acción no permitida',
        text: 'No se pueden ver las inscripciones de un evento cancelado.',
        icon: 'warning',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#cc5500'
      });
      return;
    }
    this.router.navigate(['/registrations', eventId]);
  }

  onCancelEvent(eventId: number): void {
    const event = this.events.find(e => e.id === eventId);
    if (event && event.status === EventStatus.CANCELADO) {
      Swal.fire({
        title: 'Evento ya cancelado',
        text: 'El evento ya está cancelado.',
        icon: 'info',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#cc5500'
      });
      return;
    }
    this.cancelEventIdToProcess = eventId;
    this.cancelPromptVisible = true;
  }

  // MÉTODO CORREGIDO: Ahora cierra el modal correctamente y usa SweetAlert
  confirmCancel(reason: string): void {
    const dto: CancelEventRequestDto = { cancellationReason: reason };
    const organizerId = this.userContext.userId!;

    this.eventService.cancelEvent(this.cancelEventIdToProcess, organizerId, dto).subscribe({
      next: () => {
        // ✅ CERRAR EL MODAL PRIMERO
        this.cancelPromptVisible = false;
        
        // ✅ Mostrar mensaje de éxito con SweetAlert
        Swal.fire({
          title: '¡Evento cancelado!',
          text: 'El evento ha sido cancelado exitosamente.',
          icon: 'success',
          confirmButtonText: 'Perfecto',
          confirmButtonColor: '#28a745'
        });
        
        // ✅ Recargar los eventos para mostrar el estado actualizado
        this.ngOnInit();
      },
      error: err => {
        const message = err?.error?.message || 'Ocurrió un error al cancelar el evento.';
        
        // ✅ CERRAR EL MODAL INCLUSO SI HAY ERROR
        this.cancelPromptVisible = false;
        
        // ✅ Mostrar error con SweetAlert
        Swal.fire({
          title: 'Error al cancelar',
          text: message,
          icon: 'error',
          confirmButtonText: 'Entendido',
          confirmButtonColor: '#dc3545'
        });
      }
    });
  }

  // ✅ MÉTODO AGREGADO: Para manejar el cierre manual del modal
  onModalClose(): void {
    this.cancelPromptVisible = false;
  }
}