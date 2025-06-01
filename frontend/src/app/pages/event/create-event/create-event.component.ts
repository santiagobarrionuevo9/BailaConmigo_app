import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EventService } from '../../../services/event.service';
import { CreateEventRequestDto } from '../../../models/createeventrequest';
import { CommonModule } from '@angular/common';
import { UserContextService } from '../../../services/user-context.service';
import Swal from 'sweetalert2';
import { AuthService } from '../../../services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { Country } from '../../../models/Country';
import { City } from '../../../models/City';
import { LocationService } from '../../../services/location.service';

@Component({
  selector: 'app-create-event',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-event.component.html',
  styleUrl: './create-event.component.css'
})
export class CreateEventComponent implements OnInit {
  form: FormGroup;
  availableDanceStyles: string[] = [];
  selectedStyles: string[] = [];
  countries: Country[] = [];
  cities: City[] = [];
  
  constructor(private fb: FormBuilder,private locationService: LocationService, private eventService: EventService,private router: Router, private userContext: UserContextService, private authService: AuthService) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      dateTime: ['', Validators.required],
      
      countryId: ['', Validators.required],
      cityId: ['', Validators.required],
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

  ngOnInit(): void {
    this.loadCountries();
    this.authService.getDanceStyles().subscribe(styles => {
      this.availableDanceStyles = styles;
    });
  }

  loadCountries(): void {
    this.locationService.getAllCountries().subscribe({
      next: (data) => this.countries = data,
      error: () => Swal.fire('Error', 'No se pudieron cargar los países.', 'error')
    });
  }

  onCountryChange(): void {
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
  
  
  submit(): void {
    if (this.form.invalid) return;
  
    const dto: CreateEventRequestDto = this.form.value;
    const organizerId = this.userContext.userId!;
  
    this.eventService.createEvent(organizerId, dto).subscribe({
      next: (response) => {
        Swal.fire({
          icon: 'success',
          title: '¡Evento creado!',
          text: response || 'El evento se creó exitosamente.',
          confirmButtonColor: '#b34700',
        }).then(() => {
          this.router.navigate(['/events']);
        });
        this.form.reset();
        this.selectedStyles = []; // limpiar estilos también
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: 'Error al crear el evento',
          text: err?.error || err.message || 'Error desconocido',
          confirmButtonColor: '#d33',
        }).then(() => {
          this.router.navigate(['/events']);
        });
        console.error('Error al crear el evento:', err);
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

  // Método para volver a la página anterior
  goBack(): void {
    this.router.navigate(['/events']);
  }
  
}
