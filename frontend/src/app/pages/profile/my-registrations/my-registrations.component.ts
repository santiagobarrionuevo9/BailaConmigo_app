import { Component, OnInit } from '@angular/core';
import { EventRegistrationResponseDto } from '../../../models/EventRegistrationResponseDto';
import { EventRegistrationService } from '../../../services/event-registration.service';
import { UserContextService } from '../../../services/user-context.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-my-registrations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-registrations.component.html',
  styleUrl: './my-registrations.component.css'
})
export class MyRegistrationsComponent implements OnInit {
  registrations: EventRegistrationResponseDto[] = [];
  dancerId: number =0; // Cambiar por el ID real (ej: desde el token o session)

  constructor(private registrationService: EventRegistrationService, private user: UserContextService) {}

  ngOnInit(): void {
    this.dancerId = this.user.userId || 0; // Asegurarse de que dancerId tenga un valor válido
    this.loadRegistrations();
    
  }

  loadRegistrations(): void {
    this.registrationService.getRegistrationsByDancer(this.dancerId).subscribe({
      next: (data) => this.registrations = data,
      error: (err) => console.error('Error al cargar las inscripciones', err)
    });
  }
}
