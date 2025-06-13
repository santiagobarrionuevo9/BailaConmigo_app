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
import { Country } from '../../../models/Country';
import { City } from '../../../models/City';
import { LocationService } from '../../../services/location.service';
import { forkJoin } from 'rxjs';

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
  countries: Country[] = [];
  cities: City[] = [];
  originalDateTime?: string;

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private route: ActivatedRoute,
    private router: Router,
    private userContext: UserContextService,
    private authService: AuthService,
    private locationService: LocationService
  ) {}

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id'));
    this.initializeForm();
    
    // Cargar países, estilos de baile y evento de forma simultánea
    forkJoin({
      countries: this.locationService.getAllCountries(),
      danceStyles: this.authService.getDanceStyles(),
      event: this.eventService.getEventById(this.eventId)
    }).subscribe({
      next: (data) => {
        console.log('Data loaded successfully:', data);
        this.countries = data.countries;
        this.availableDanceStyles = data.danceStyles;
        this.loadEventData(data.event);
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: 'Error al cargar los datos',
          text: err?.error || err.message || 'Error desconocido',
          confirmButtonColor: '#d33',
        });
        console.error('Error al cargar los datos:', err);
      }
    });
  }

  initializeForm(): void {
    this.form = this.fb.group({
      name: [{value: '', disabled: true}, Validators.required],
      description: [''],
      dateTime: [{value: '', disabled: true}, Validators.required],
      location: [''],
      countryId: [{value: null, disabled: true}, Validators.required],
      cityId: [{value: null, disabled: true}, Validators.required],
      address: [''],
      capacity: [1, [Validators.min(0)]],
      price: [{value: 0, disabled: true}, [Validators.min(0)]],
      eventType: ['CLASE'],
      danceStyles: [[]],
      additionalInfo: ['']
    });
  }

  loadEventData(event: EventResponseDto): void {
    this.originalEvent = event;
    this.originalDateTime = event.dateTime;
    
    // Debug: Ver qué estructura tienen los danceStyles
    console.log('Event danceStyles:', event.danceStyles);
    console.log('Available dance styles:', this.availableDanceStyles);
    
    // Resetear selectedStyles antes de procesar
    this.selectedStyles = [];
    
    // Convertir estilos de baile - manejar diferentes estructuras posibles
    if (event.danceStyles && Array.isArray(event.danceStyles)) {
      this.selectedStyles = event.danceStyles
        .map(style => {
          // Si es un objeto con propiedad 'name'
          if (typeof style === 'object' && style.name) {
            return style.name;
          }
          // Si es directamente un string
          if (typeof style === 'string') {
            return style;
          }
          // Si tiene otras propiedades, intentar diferentes nombres comunes
          if (typeof style === 'object') {
            return style.name || style.id || null;
          }
          return null;
        })
        .filter((styleName): styleName is string => typeof styleName === 'string' && styleName.trim() !== ''); // Filtrar valores nulos/vacíos y asegurar solo strings
    }
    
    console.log('Processed selectedStyles:', this.selectedStyles);
    
    // Actualizar el formulario con todos los datos
    this.form.patchValue({
      name: event.name,
      description: event.description || '',
      dateTime: event.dateTime,
      address: event.address || '',
      capacity: event.capacity || 0,
      countryId: event.countryId || null,
      cityId: event.cityId || null,
      price: event.price || 0,
      eventType: event.eventType || 'CLASE',
      danceStyles: this.selectedStyles,
      additionalInfo: event.additionalInfo || ''
    });

    // Cargar ciudades para mostrar la ciudad actual
    if (event.countryId) {
      this.loadCitiesForCountry(event.countryId);
    }
  }

  loadCitiesForCountry(countryId: number): void {
    this.locationService.getCitiesByCountry(countryId).subscribe({
      next: (data) => this.cities = data,
      error: () => console.error('Error al cargar ciudades')
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    // Crear el DTO solo con los campos que pueden ser modificados
    const dto: EditEventRequestDto = {
      name: this.originalEvent?.name || '',
      description: this.form.value.description,
      dateTime: this.originalEvent?.dateTime || '',
      address: this.form.value.address,
      capacity: this.form.value.capacity,
      price: this.originalEvent?.price || 0,
      danceStyles: this.selectedStyles,
      additionalInfo: this.form.value.additionalInfo,
      countryId: this.originalEvent?.countryId || 0,
      cityId: this.originalEvent?.cityId || 0
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

  addDanceStyle(style: string): void {
    if (!style || style.trim() === '' || this.selectedStyles.includes(style)) {
      console.log('Style not added:', style, 'Reason: empty or already exists');
      return;
    }
    
    // Verificar que el estilo existe en los disponibles
    if (!this.availableDanceStyles.includes(style)) {
      console.log('Style not available:', style);
      return;
    }
    
    this.selectedStyles.push(style);
    this.form.patchValue({ danceStyles: [...this.selectedStyles] });
    console.log('Style added:', style, 'Current styles:', this.selectedStyles);
  }

  removeDanceStyle(index: number): void {
    this.selectedStyles.splice(index, 1);
    this.form.patchValue({ danceStyles: [...this.selectedStyles] });
  }

  onCountryChange(): void {
    // Este método ya no es necesario ya que los campos están deshabilitados
    // Pero lo mantenemos por compatibilidad
    const selectedCountryId = this.form.get('countryId')?.value;
    if (selectedCountryId) {
      this.locationService.getCitiesByCountry(selectedCountryId).subscribe({
        next: (data) => this.cities = data,
        error: () => Swal.fire('Error', 'No se pudieron cargar las ciudades.', 'error')
      });
    } else {
      this.cities = [];
      this.form.get('cityId')?.setValue('');
    }
  }

  goBack(): void {
    this.router.navigate(['/events']);
  }

  get f() {
    return this.form.controls;
  }
}