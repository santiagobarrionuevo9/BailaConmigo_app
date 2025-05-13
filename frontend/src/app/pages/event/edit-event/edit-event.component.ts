import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { UserContextService } from '../../../services/user-context.service';
import { EditEventRequestDto } from '../../../models/EditEventRequestDto';
import { EventResponseDto } from '../../../models/eventresponse';
import { CommonModule } from '@angular/common';
import Swal from 'sweetalert2';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-edit-event',
  standalone: true,
  imports: [ReactiveFormsModule,CommonModule],
  templateUrl: './edit-event.component.html',
  styleUrl: './edit-event.component.css'
})
export class EditEventComponent implements OnInit {
  form!: FormGroup;
  eventId!: number;
  availableDanceStyles: string[] = [];
  selectedStyles: string[] = [];
  originalEvent?: EventResponseDto;

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private route: ActivatedRoute,
    private router: Router,
    private userContext: UserContextService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id'));

    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      dateTime: ['', Validators.required],
      location: [''],
      address: [''],
      capacity: [1, [Validators.min(0)]],
      price: [0, [Validators.min(0)]],
      eventType: ['CLASE'],
      danceStyles: [[]],
      additionalInfo: ['']
    });

    // Cargar estilos de baile disponibles
    this.authService.getDanceStyles().subscribe(styles => {
      this.availableDanceStyles = styles;
    });

    this.loadEvent();
  }

  loadEvent(): void {
    this.eventService.getEventById(this.eventId).subscribe({
      next: (event: EventResponseDto) => {
        this.originalEvent = event;
        
        // Convertir estilos de baile de objetos a nombres
        if (event.danceStyles && Array.isArray(event.danceStyles)) {
          this.selectedStyles = event.danceStyles.map(style => style.name);
        }
        
        // Actualizar el formulario
        this.form.patchValue({
          name: event.name,
          description: event.description || '',
          dateTime: event.dateTime,
          location: event.location || '',
          address: event.address || '',
          capacity: event.capacity || 0,
          price: event.price || 0,
          eventType: event.eventType || 'CLASE',
          danceStyles: this.selectedStyles,
          additionalInfo: event.additionalInfo || ''
        });
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: 'Error al cargar el evento',
          text: err?.error || err.message || 'Error desconocido',
          confirmButtonColor: '#d33',
        });
        console.error('Error al cargar el evento:', err);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    // Asegurarse de que estamos enviando solo los campos permitidos en EditEventRequestDto
    const dto: EditEventRequestDto = {
      name: this.form.value.name,
      description: this.form.value.description,
      dateTime: this.form.value.dateTime,
      location: this.form.value.location,
      address: this.form.value.address,
      capacity: this.form.value.capacity,
      price: this.form.value.price,
      danceStyles: this.selectedStyles,
      additionalInfo: this.form.value.additionalInfo
    };
    
    const organizerId = this.userContext.userId!;

    console.log('Editando evento:', this.eventId, 'con datos:', dto);
    this.eventService.editEvent(this.eventId, organizerId, dto).subscribe({
      next: (response) => {
        Swal.fire({
          icon: 'success',
          title: '¡Evento actualizado!',
          text: response || 'El evento se actualizó exitosamente.',
          confirmButtonColor: '#b34700',
        }).then(() => {
          this.router.navigate(['/events']);
        });
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: 'Error al actualizar el evento',
          text: err?.error || err.message || 'Error desconocido',
          confirmButtonColor: '#d33',
        });
        console.error('Error al actualizar el evento:', err);
      }
    });
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

  // Método para volver a la página anterior
  goBack(): void {
    this.router.navigate(['/events']);
  }

  get f() {
    return this.form.controls;
  }
}