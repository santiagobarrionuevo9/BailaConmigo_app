import { Component, OnInit } from '@angular/core';
import { EventResponseDto } from '../../../models/eventresponse';
import { EventService } from '../../../services/event.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-organizer-event',
  standalone: true,
  imports: [  CommonModule, RouterLink],
  templateUrl: './organizer-event.component.html',
  styleUrl: './organizer-event.component.css'
})
export class OrganizerEventComponent implements OnInit {
    events: EventResponseDto[] = [];
    organizerId: number = 1; // Reemplazar con el ID real del organizador autenticado
  
    constructor(private eventService: EventService) {}
  
    ngOnInit(): void {
      this.eventService.getEventsByOrganizer(this.organizerId).subscribe((res) => {
        this.events = res;
      });
    }
}
