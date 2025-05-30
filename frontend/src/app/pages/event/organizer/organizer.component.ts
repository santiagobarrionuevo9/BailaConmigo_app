import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { OrganizerProfileResponseDto } from '../../../models/OrganizerProfileResponseDto';
import { Country } from '../../../models/Country';
import { City } from '../../../models/City';
import { OrganizerService } from '../../../services/organizer.service';
import { LocationService } from '../../../services/location.service';
import { UserContextService } from '../../../services/user-context.service';
import Swal from 'sweetalert2';
import { EditOrganizerProfileDto } from '../../../models/EditOrganizerProfileDto';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-organizer',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule],
  templateUrl: './organizer.component.html',
  styleUrl: './organizer.component.css'
})
export class OrganizerComponent implements OnInit {
  form!: FormGroup;
  userId!: number;
  profileData!: OrganizerProfileResponseDto;
  isEditing = false;
  mediaPreview: string[] = [];
  selectedFileNames: Set<string> = new Set();
  
  // Para ubicación
  countries: Country[] = [];
  cities: City[] = [];
  selectedCountryId?: number;
  selectedCityId?: number;

  constructor(
    private fb: FormBuilder,
    private organizerService: OrganizerService,
    private locationService: LocationService,
    private userContext: UserContextService
  ) {}

  ngOnInit(): void {
    this.userId = this.userContext.userId!;
    this.initializeForm();
    this.loadCountries();
    this.loadProfile();
  }

  initializeForm(): void {
    this.form = this.fb.group({
      organizationName: ['', [Validators.required, Validators.minLength(2)]],
      contactEmail: ['', [Validators.required, Validators.email]],
      contactPhone: ['', [Validators.required]],
      description: [''],
      website: [''],
      countryId: ['', [Validators.required]],
      cityId: ['', [Validators.required]],
      mediaUrls: [[]]
    });
  }

  loadProfile(): void {
  this.organizerService.getOrganizerProfile(this.userId).subscribe({
    next: (profile) => {
      this.profileData = profile;
      this.mediaPreview = [...profile.mediaUrls];
      this.initializeSelectedFiles();
      
      // Si tenemos datos del perfil, buscar los IDs de país y ciudad
      // PERO solo después de que los países estén cargados
      if (profile.countryName && this.countries.length > 0) {
        this.findCountryAndCityIds(profile.countryName, profile.cityName);
      } else if (profile.countryName) {
        // Si los países no están cargados aún, esperar a que se carguen
        this.waitForCountriesAndSetIds(profile.countryName, profile.cityName);
      }
    },
    error: (error) => {
      console.error('Error cargando perfil:', error);
      Swal.fire({
        icon: 'error',
        title: 'Error',
        text: 'No se pudo cargar el perfil del organizador',
        confirmButtonColor: '#ff6600'
      });
    }
  });
}

loadCountries(): void {
  this.locationService.getAllCountries().subscribe({
    next: (countries) => {
      this.countries = countries;
      
      // Si ya tenemos el perfil cargado y necesitamos establecer los IDs
      if (this.profileData?.countryName && !this.selectedCountryId) {
        this.findCountryAndCityIds(this.profileData.countryName, this.profileData.cityName);
      }
    },
    error: (error) => {
      console.error('Error cargando países:', error);
    }
  });
}

// Método mejorado para esperar a que se carguen los países
private waitForCountriesAndSetIds(countryName: string, cityName: string): void {
  const checkCountries = () => {
    if (this.countries.length > 0) {
      this.findCountryAndCityIds(countryName, cityName);
    } else {
      // Reintentar después de 100ms
      setTimeout(checkCountries, 100);
    }
  };
  checkCountries();
}

onCountryChange(event: Event): void {
  const target = event.target as HTMLSelectElement;
  const countryId = +target.value;
  
  this.selectedCountryId = countryId;
  this.selectedCityId = undefined;
  this.cities = []; // Limpiar ciudades inmediatamente
  this.form.patchValue({ cityId: '' });
  
  if (countryId) {
    this.locationService.getCitiesByCountry(countryId).subscribe({
      next: (cities) => {
        this.cities = cities;
      },
      error: (error) => {
        console.error('Error cargando ciudades:', error);
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'Error al cargar las ciudades',
          confirmButtonColor: '#ff6600'
        });
      }
    });
  }
}

onCityChange(event: Event): void {
  const target = event.target as HTMLSelectElement;
  this.selectedCityId = +target.value;
}

// Método mejorado para encontrar IDs de país y ciudad
findCountryAndCityIds(countryName: string, cityName: string): void {
  if (!this.countries || this.countries.length === 0) {
    console.warn('Countries not loaded yet');
    return;
  }

  // Buscar el país por nombre
  const country = this.countries.find(c => c.name === countryName);
  if (country) {
    this.selectedCountryId = country.id;
    
    // Cargar ciudades del país encontrado
    this.locationService.getCitiesByCountry(country.id).subscribe({
      next: (cities) => {
        this.cities = cities;
        
        // Buscar la ciudad por nombre
        const city = this.cities.find(c => c.name === cityName);
        if (city) {
          this.selectedCityId = city.id;
        }
      },
      error: (error) => {
        console.error('Error cargando ciudades para establecer perfil:', error);
      }
    });
  }
}

toggleEdit(): void {
  this.isEditing = !this.isEditing;
    if (this.isEditing && this.profileData) {
      // Asegurar que tenemos los IDs correctos antes de hacer patch
      if (this.profileData.countryName && !this.selectedCountryId) {
        this.findCountryAndCityIds(this.profileData.countryName, this.profileData.cityName);
      }
      
      // Hacer patch de los valores del formulario
      this.form.patchValue({
        organizationName: this.profileData.organizationName,
        contactEmail: this.profileData.contactEmail,
        contactPhone: this.profileData.contactPhone,
        description: this.profileData.description,
        website: this.profileData.website,
        mediaUrls: this.profileData.mediaUrls
      });
      
      // Establecer los IDs de país y ciudad después de un breve delay
      // para asegurar que los datos estén disponibles
      setTimeout(() => {
        this.form.patchValue({
          countryId: this.selectedCountryId || '',
          cityId: this.selectedCityId || ''
        });
      }, 100);
      
      this.mediaPreview = [...this.profileData.mediaUrls];
    } else {
      // Al cancelar, limpiar las ciudades si no hay país seleccionado
      if (!this.selectedCountryId) {
        this.cities = [];
      }
    }
  }

  

  onSubmit(): void {
    if (this.form.invalid) {
      this.markFormGroupTouched();
      return;
    }

    const formValue = this.form.getRawValue();
    const dto: EditOrganizerProfileDto = {
      organizationName: formValue.organizationName,
      contactEmail: formValue.contactEmail,
      contactPhone: formValue.contactPhone,
      description: formValue.description,
      website: formValue.website,
      cityId: formValue.cityId,
      countryId: formValue.countryId,
      mediaUrls: this.mediaPreview
    };

    this.organizerService.updateOrganizerProfile(this.userId, dto).subscribe({
      next: (successMessage) => {
        Swal.fire({
          icon: 'success',
          title: 'Éxito',
          text: 'Perfil actualizado correctamente',
          confirmButtonColor: '#b34700',
          showConfirmButton: false,
          timer: 1500
        });
        
        // Actualizar datos locales y cerrar modo edición
        this.loadProfile();
        this.isEditing = false;
      },
      error: (error) => {
        let errorMessage = 'Error al actualizar el perfil';
        if (error && error.message) {
          errorMessage = error.message;
        }
        
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: errorMessage,
          confirmButtonColor: '#ff6600'
        });
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      // Verificar si el archivo ya fue seleccionado
      if (this.selectedFileNames.has(file.name)) {
        Swal.fire({
          icon: 'warning',
          title: 'Archivo duplicado',
          text: 'Ya has seleccionado este archivo anteriormente.',
          confirmButtonColor: '#ff6600'
        });
        return;
      }

      const formData = new FormData();
      formData.append('file', file);

      this.organizerService.uploadMedia(formData).subscribe({
        next: (url: string) => {
          if (!this.mediaPreview.includes(url)) {
            this.mediaPreview.push(url);
            this.selectedFileNames.add(file.name);
          }
        },
        error: (error) => {
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Error al subir el archivo',
            confirmButtonColor: '#ff6600'
          });
        }
      });
    }
  }

  removeMedia(index: number): void {
    const url = this.mediaPreview[index];
    const fileName = this.extractFileNameFromUrl(url);
    if (fileName) {
      this.selectedFileNames.delete(fileName);
    }
    
    this.mediaPreview.splice(index, 1);
  }

  initializeSelectedFiles(): void {
    if (this.profileData && this.profileData.mediaUrls) {
      this.profileData.mediaUrls.forEach(url => {
        const fileName = this.extractFileNameFromUrl(url);
        if (fileName) {
          this.selectedFileNames.add(fileName);
        }
      });
    }
  }

  extractFileNameFromUrl(url: string): string | null {
    const match = url.match(/[^/]+_([^/]+)$/);
    return match ? match[1] : null;
  }

  isVideo(url: string): boolean {
    return /\.(mp4|mov|avi|webm|ogg)$/i.test(url);
  }

  isImage(url: string): boolean {
    return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.form.controls).forEach(key => {
      const control = this.form.get(key);
      control?.markAsTouched();
    });
  }

  // Getters para validación en template
  get organizationName() { return this.form.get('organizationName'); }
  get contactEmail() { return this.form.get('contactEmail'); }
  get contactPhone() { return this.form.get('contactPhone'); }
  get countryId() { return this.form.get('countryId'); }
  get cityId() { return this.form.get('cityId'); }

  
}
