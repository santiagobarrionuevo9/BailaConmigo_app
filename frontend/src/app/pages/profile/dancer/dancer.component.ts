import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import {  FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ProfileService } from '../../../services/profile.service';
import { EditDancerProfileDto } from '../../../models/editdancerprofile';
import { CommonModule } from '@angular/common';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import Swal from 'sweetalert2';
import { UserContextService } from '../../../services/user-context.service';
import { AuthstateService } from '../../../services/authstate.service';
import { AuthService } from '../../../services/auth.service';
import { Country } from '../../../models/Country';
import { City } from '../../../models/City';
import { LocationService } from '../../../services/location.service';

@Component({
  selector: 'app-dancer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dancer.component.html',
  styleUrl: './dancer.component.css'
})
export class DancerComponent implements OnInit {
  form!: FormGroup;
  userId!: number;
  profileData!: DancerProfileResponseDto;
  isEditing = false;
  mediaPreview: string[] = [];
  currentMediaIndex = 0; // Para control de carrusel tipo Tinder
  selectedFileNames: Set<string> = new Set();
  countries: Country[] = [];
  cities: City[] = [];
  processedMediaUrls: string[] = [];
isLoadingMedia = false;

  
  availableDanceStyles: string[] = [];
  selectedStyles: string[] = [];

  constructor(
    private fb: FormBuilder, 
    private profileService: ProfileService,
    private userContext: UserContextService, 
    private auhtservice: AuthService,
    private locationService: LocationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.locationService.getAllCountries().subscribe({
    next: (countries) => this.countries = countries
  });

    

    this.userId = this.userContext.userId!;
    this.form = this.fb.group({
      countryId: [null],
      cityId: [null],
      danceStyles: [[]],
      level: [''],
      aboutMe: [''],
      availability: [''],
      mediaUrls: [[]]
    });
    
    this.auhtservice.getDanceStyles().subscribe(styles => {
      this.availableDanceStyles = styles;
    });

  this.form.get('countryId')?.valueChanges.subscribe((countryId: number) => {
  if (!countryId) {
    this.cities = [];
    this.form.patchValue({ cityId: null });
    return;
  }

  this.locationService.getCitiesByCountry(countryId).subscribe({
    next: (cities) => {
      this.cities = cities;
      this.form.patchValue({ cityId: null }); // Reinicia ciudad al cambiar país
    }
  });
});

  this.profileService.getProfile(this.userId).subscribe({
    next: (profile) => {
      console.log('=== PROFILE RECEIVED ===');
      console.log('Profile:', profile);
      
      this.profileData = profile;
      this.form.patchValue(profile);
      
      if (profile.mediaUrls && Array.isArray(profile.mediaUrls)) {
        // Procesar URLs para ngrok
        this.processMediaUrls(profile.mediaUrls);
      } else {
        this.mediaPreview = [];
      }
      
      this.selectedStyles = [...profile.danceStyles];
      this.initializeSelectedFiles();

      if (profile.countryId) {
        this.locationService.getCitiesByCountry(profile.countryId).subscribe({
          next: (cities) => this.cities = cities
        });
      }
      
      this.cdr.detectChanges();
    },
    error: (error) => {
      console.error('Error loading profile:', error);
    }
  });

  }

  

// Método para crear URLs seguras para ngrok
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

// Método mejorado para manejo de errores
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

// Limpiar URLs de objeto cuando el componente se destruye
ngOnDestroy(): void {
  // Limpiar URLs de objeto para evitar memory leaks
  this.processedMediaUrls.forEach(url => {
    if (url.startsWith('blob:')) {
      URL.revokeObjectURL(url);
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
  
  // También asegúrate de que estos métodos estén correctos:
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

// Método para debugging - puedes llamarlo desde la consola del navegador
debugMediaPreview(): void {
  console.log('=== DEBUG MEDIA PREVIEW ===');
  console.log('mediaPreview array:', this.mediaPreview);
  console.log('currentMediaIndex:', this.currentMediaIndex);
  console.log('URL actual:', this.mediaPreview[this.currentMediaIndex]);
  console.log('¿Es video?', this.isVideo(this.mediaPreview[this.currentMediaIndex]));
  console.log('¿Es imagen?', this.isImage(this.mediaPreview[this.currentMediaIndex]));
}

  


  // Extract filenames from existing URLs to prevent duplicates
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

  // Extract the original filename from the URL
  extractFileNameFromUrl(url: string): string | null {
    const match = url.match(/[^/]+_([^/]+)$/);
    return match ? match[1] : null;
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (this.isEditing) {
      this.form.patchValue(this.profileData);
      this.mediaPreview = [...this.profileData.mediaUrls];
      // Restablecer los estilos seleccionados cuando se entra en modo edición
      this.selectedStyles = [...this.profileData.danceStyles];
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const dto: EditDancerProfileDto = this.form.getRawValue();

    this.profileService.updateProfile(this.userId, dto).subscribe({
      next: (successMessage) => {
        // La operación fue exitosa, mostramos el mensaje de éxito
        Swal.fire({
          icon: 'success',
          title: 'Éxito',
          text: successMessage,
          confirmButtonColor: '#b34700',
          showConfirmButton: false,
          timer: 1500
        });
        
        // Actualizamos los datos locales y cerramos el modo de edición
        this.profileData = { ...this.profileData, ...dto };
        
        this.isEditing = false;
      },
      error: (error) => {
        // Extraemos el mensaje de error
        let errorMessage = 'Error al actualizar el perfil';
        
        if (error && error.error) {
          errorMessage = typeof error.error === 'string' ? error.error : errorMessage;
        }
        
        // Mostramos el mensaje de error
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: errorMessage,
          confirmButtonColor: '#ff6600',
          showConfirmButton: true
        });
      }
    });
  }

  // Método onFileSelected ACTUALIZADO
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

    this.profileService.uploadMedia(formData).subscribe({
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
          
          // ← FORZAR DETECCIÓN DE CAMBIOS
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

  // También actualizar el método removeMedia
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

  

  onCountryChange(countryId: number): void {
  if (!countryId) {
    this.cities = [];
    this.form.patchValue({ cityId: null });
    return;
  }

  this.locationService.getCitiesByCountry(countryId).subscribe({
    next: (cities) => {
      this.cities = cities;
      this.form.patchValue({ cityId: null });
    }
  });
}


  isVideo(url: string): boolean {
    return /\.(mp4|mov|avi|webm|ogg)$/i.test(url);
  }

  isImage(url: string): boolean {
    return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
  }

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
  this.isLoadingMedia = false;
  this.cdr.detectChanges();
}
}