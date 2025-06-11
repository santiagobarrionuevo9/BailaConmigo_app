import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
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
import { ProfileService } from '../../../services/profile.service';

@Component({
  selector: 'app-organizer',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule],
  templateUrl: './organizer.component.html',
  styleUrl: './organizer.component.css'
})
export class OrganizerComponent implements OnInit, OnDestroy {
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

  // Nuevas propiedades para manejo de media mejorado
  currentMediaIndex = 0;
  processedMediaUrls: string[] = [];
  isLoadingMedia = false;

  constructor(
    private fb: FormBuilder,
    private organizerService: OrganizerService,
    private locationService: LocationService,
    private userContext: UserContextService,
    private profileService: ProfileService,
    private cdr: ChangeDetectorRef // ← Agregar ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userId = this.userContext.userId!;
    this.initializeForm();
    this.loadCountries();
    this.loadProfile();
  }

  // Implementar OnDestroy para limpiar URLs de objeto
  ngOnDestroy(): void {
    // Limpiar URLs de objeto para evitar memory leaks
    this.processedMediaUrls.forEach(url => {
      if (url.startsWith('blob:')) {
        URL.revokeObjectURL(url);
      }
    });
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
        console.log('=== ORGANIZER PROFILE RECEIVED ===');
        console.log('Profile:', profile);
        
        this.profileData = profile;
        
        // Procesar URLs de media como en dancer component
        if (profile.mediaUrls && Array.isArray(profile.mediaUrls)) {
          this.processMediaUrls(profile.mediaUrls);
        } else {
          this.mediaPreview = [];
        }
        
        this.initializeSelectedFiles();
        
        // Si tenemos datos del perfil, buscar los IDs de país y ciudad
        if (profile.countryName && this.countries.length > 0) {
          this.findCountryAndCityIds(profile.countryName, profile.cityName);
        } else if (profile.countryName) {
          this.waitForCountriesAndSetIds(profile.countryName, profile.cityName);
        }
        
        this.cdr.detectChanges();
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

  // Método para procesar URLs de media (copiado del dancer component)
  async processMediaUrls(urls: string[]): Promise<void> {
    this.isLoadingMedia = true;

    const processedUrls = await Promise.all(urls.map(async url => {
      try {
        if (url.includes('ngrok')) {
          return await this.profileService.createObjectURL(url);
        } else {
          return url;
        }
      } catch (error) {
        console.error('Error procesando URL:', url, error);
        return url;
      }
    }));

    this.mediaPreview = processedUrls;
    this.processedMediaUrls = processedUrls;
    this.isLoadingMedia = false;
    this.cdr.detectChanges();
  }

  // Método para crear URLs seguras para ngrok (copiado del dancer component)
  createNgrokSafeUrl(url: string): Promise<string> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.crossOrigin = 'anonymous';
      
      // Agregar header para ngrok
      fetch(url, {
        headers: {
          'ngrok-skip-browser-warning': 'true'
        }
      })
      .then(response => response.blob())
      .then(blob => {
        const objectURL = URL.createObjectURL(blob);
        resolve(objectURL);
      })
      .catch(error => {
        console.error('Error creando URL segura:', error);
        reject(error);
      });
    });
  }

  // Método mejorado para manejo de errores de imagen (copiado del dancer component)
  onImageError(event: any): void {
    console.error('Error cargando imagen:', event.target.src);
    
    // Intentar recargar con headers de ngrok
    const originalUrl = event.target.src;
    if (originalUrl.includes('ngrok') && !originalUrl.startsWith('blob:')) {
      console.log('Intentando recargar con headers de ngrok...');
      this.createNgrokSafeUrl(originalUrl)
        .then(safeUrl => {
          event.target.src = safeUrl;
        })
        .catch(error => {
          console.error('No se pudo crear URL segura:', error);
          // Mostrar imagen de placeholder
          event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0yMCAyNUMyMi43NjE0IDI1IDI1IDIyLjc2MTQgMjUgMjBDMjUgMTcuMjM4NiAyMi43NjE0IDE1IDIwIDE1QzE3LjIzODYgMTUgMTUgMTcuMjM4NiAxNSAyMEMxNSAyMi43NjE0IDE3LjIzODYgMjUgMjAgMjVaIiBmaWxsPSIjOUM5Qzk3Ii8+CjxwYXRoIGQ9Ik0xMCAzMEgzMEwyNSAyMEwyMCAyNUwxNSAyMEwxMCAzMFoiIGZpbGw9IiM5QzlDOTciLz4KPC9zdmc+';
        });
    }
  }

  // Métodos para navegación de media (copiados del dancer component)
  nextMedia(): void {
    if (this.mediaPreview.length === 0) return;
    this.currentMediaIndex = (this.currentMediaIndex + 1) % this.mediaPreview.length;
    console.log('Siguiente media, índice actual:', this.currentMediaIndex);
  }

  previousMedia(): void {
    if (this.mediaPreview.length === 0) return;
    this.currentMediaIndex = (this.currentMediaIndex - 1 + this.mediaPreview.length) % this.mediaPreview.length;
    console.log('Media anterior, índice actual:', this.currentMediaIndex);
  }

  // Método para debugging (copiado del dancer component)
  debugMediaPreview(): void {
    console.log('=== DEBUG MEDIA PREVIEW ===');
    console.log('mediaPreview array:', this.mediaPreview);
    console.log('currentMediaIndex:', this.currentMediaIndex);
    console.log('URL actual:', this.mediaPreview[this.currentMediaIndex]);
    console.log('¿Es video?', this.isVideo(this.mediaPreview[this.currentMediaIndex]));
    console.log('¿Es imagen?', this.isImage(this.mediaPreview[this.currentMediaIndex]));
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
      setTimeout(() => {
        this.form.patchValue({
          countryId: this.selectedCountryId || '',
          cityId: this.selectedCityId || ''
        });
      }, 100);
      
      // Copiar el array de mediaPreview como en dancer component
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

  // Método onFileSelected mejorado (adaptado del dancer component)
  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (!file) return;

    console.log('Archivo seleccionado:', file.name);

    if (this.selectedFileNames.has(file.name)) {
      Swal.fire({
        icon: 'warning',
        title: 'Archivo duplicado',
        text: 'Ya has seleccionado este archivo anteriormente.',
        confirmButtonColor: '#ff6600',
      });
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    console.log('Enviando archivo al backend...');

    this.organizerService.uploadMedia(formData).subscribe({
      next: (response: string) => {
        console.log('Respuesta del backend:', response);
        
        const url = response.trim();
        console.log('URL limpia:', url);
        
        const currentUrls = this.form.value.mediaUrls || [];
        console.log('URLs actuales:', currentUrls);
        
        if (!currentUrls.includes(url)) {
          const newList = [...currentUrls, url];
          console.log('Nueva lista de URLs:', newList);
          
          // ACTUALIZAR TANTO EL FORMULARIO COMO LA VISTA
          this.form.patchValue({ mediaUrls: newList });
          this.mediaPreview = [...newList]; // Crear nuevo array
          
          console.log('mediaPreview actualizado:', this.mediaPreview);
          
          this.selectedFileNames.add(file.name);
          this.currentMediaIndex = this.mediaPreview.length - 1;
          
          // FORZAR DETECCIÓN DE CAMBIOS
          this.cdr.detectChanges();
          
          console.log('Change detection forzado');
          
          Swal.fire({
            icon: 'success',
            title: 'Archivo subido exitosamente',
            showConfirmButton: false,
            timer: 1500
          });
        }
      },
      error: (error) => {
        console.error('Error uploading file:', error);
        Swal.fire({
          icon: 'error',
          title: 'Error al subir archivo',
          text: 'Hubo un problema al subir el archivo.',
          confirmButtonColor: '#ff6600',
        });
      }
    });

    event.target.value = '';
  }

  // Método removeMedia mejorado (adaptado del dancer component)
  removeMedia(index: number): void {
    const url = this.mediaPreview[index];
    const fileName = this.extractFileNameFromUrl(url);
    if (fileName) {
      this.selectedFileNames.delete(fileName);
    }
    
    // Crear nuevo array en lugar de mutar el existente
    this.mediaPreview = this.mediaPreview.filter((_, i) => i !== index);
    this.form.patchValue({ mediaUrls: [...this.mediaPreview] });
    
    if (this.currentMediaIndex >= this.mediaPreview.length) {
      this.currentMediaIndex = Math.max(0, this.mediaPreview.length - 1);
    }
    
    // Forzar detección de cambios
    this.cdr.detectChanges();
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