import { Component, OnInit } from '@angular/core';
import { EventResponseDto } from '../../../models/eventresponse';
import { EventService } from '../../../services/event.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserContextService } from '../../../services/user-context.service';

@Component({
  selector: 'app-organizer-event',
  standalone: true,
  imports: [  CommonModule, RouterLink],
  templateUrl: './organizer-event.component.html',
  styleUrl: './organizer-event.component.css'
})
export class OrganizerEventComponent implements OnInit {
  events: EventResponseDto[] = [];
  organizerId!: number;
  expandedEvents: boolean[] = [];
  
  constructor(private eventService: EventService,private userContext: UserContextService) {}
  
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
}
