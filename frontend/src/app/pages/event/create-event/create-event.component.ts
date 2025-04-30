import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventService } from '../../../services/event.service';
import { CreateEventRequestDto } from '../../../models/createeventrequest';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-create-event',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-event.component.html',
  styleUrl: './create-event.component.css'
})
export class CreateEventComponent {
  form: FormGroup;
  
  constructor(private fb: FormBuilder, private eventService: EventService) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      dateTime: ['', Validators.required],
      location: [''],
      address: [''],
      capacity: [0],
      price: [0],
      eventType: ['CLASE'],
      // Campos adicionales requeridos por el backend:
      danceStyles: [[]],     // Array vacío por defecto
      additionalInfo: [''],
      mediaUrls: [[]]        // Array vacío por defecto
    });
  }
  
  submit(): void {
    if (this.form.invalid) return;
    
    const dto: CreateEventRequestDto = this.form.value;
    
    // Asegurar que los valores se envían correctamente
    // Si el backend espera un conjunto para danceStyles, asegúrate de convertir el array a Set
    
    const organizerId = 1; // Reemplazar con ID real del organizador (¿obtener del usuario autenticado?)
    
    this.eventService.createEvent(organizerId, dto).subscribe({
      next: (response) => {
        alert(response || 'Evento creado exitosamente');
        this.form.reset();
      },
      error: (err) => {
        console.error('Error al crear el evento:', err);
        alert('Error al crear el evento: ' + (err.error || err.message || 'Error desconocido'));
      }
    });
  }
}
