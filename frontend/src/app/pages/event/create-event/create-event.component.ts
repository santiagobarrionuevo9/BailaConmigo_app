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
  availableDanceStyles: string[] = ['SALSA', 'BACHATA', 'KIZOMBA', 'MERENGUE', 'TANGO', 'ZOUK', 'URBANO', 'CUMBIA'];
  selectedStyles: string[] = [];
  
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

  onDanceStylesChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    const styles = value.split(',').map(s => s.trim()).filter(s => s);
    this.form.get('danceStyles')?.setValue(styles);
  }
  
  onMediaUrlsChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    const urls = value.split(',').map(s => s.trim()).filter(s => s);
    this.form.get('mediaUrls')?.setValue(urls);
  }

  // Método para agregar un estilo de baile
  addDanceStyle(style: string): void {
    if (!style || this.selectedStyles.includes(style)) return;
    
    this.selectedStyles.push(style);
    // Actualizar el campo oculto en el formulario
    this.form.patchValue({ danceStyles: [...this.selectedStyles] });
  }

  // Método para eliminar un estilo de baile
  removeDanceStyle(index: number): void {
    this.selectedStyles.splice(index, 1);
    // Actualizar el campo oculto en el formulario
    this.form.patchValue({ danceStyles: [...this.selectedStyles] });
  }
  
}
