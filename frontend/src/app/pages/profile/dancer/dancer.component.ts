import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ProfileService } from '../../../services/profile.service';
import { EditDancerProfileDto } from '../../../models/editdancerprofile';
import { CommonModule } from '@angular/common';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-dancer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dancer.component.html',
  styleUrl: './dancer.component.css'
})
export class DancerComponent implements OnInit {
  form!: FormGroup;
  userId = 2;
  profileData!: DancerProfileResponseDto;
  isEditing = false;
  mediaPreview: string[] = [];
  selectedFileNames: Set<string> = new Set();
  
  // Para el selector de estilos de baile
  availableDanceStyles: string[] = ['SALSA', 'BACHATA', 'KIZOMBA', 'MERENGUE', 'TANGO', 'ZOUK', 'URBANO', 'CUMBIA'];
  selectedStyles: string[] = [];

  constructor(private fb: FormBuilder, private profileService: ProfileService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      city: [''],
      danceStyles: [[]],
      level: [''],
      aboutMe: [''],
      availability: [''],
      mediaUrls: [[]]
    });

    this.profileService.getProfile(this.userId).subscribe(profile => {
      this.profileData = profile;
      this.form.patchValue(profile);
      this.mediaPreview = [...profile.mediaUrls];
      
      // Inicializar los estilos seleccionados
      this.selectedStyles = [...profile.danceStyles];
      
      // Inicializar los archivos seleccionados
      this.initializeSelectedFiles();
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
      this.mediaPreview = [...this.form.value.mediaUrls];
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
          showConfirmButton: true
        });
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      // Check if this file has already been selected based on name
      if (this.selectedFileNames.has(file.name)) {
        Swal.fire({
          icon: 'warning',
          title: 'Archivo duplicado',
          text: 'Ya has seleccionado este archivo anteriormente.',
          showConfirmButton: true
        });
        return;
      }

      const formData = new FormData();
      formData.append('file', file);

      this.profileService.uploadMedia(formData).subscribe((url: string) => {
        const currentUrls = this.form.value.mediaUrls || [];
        
        // Double check the URL isn't already in our list
        if (!currentUrls.includes(url)) {
          const newList = [...currentUrls, url];
          this.form.patchValue({ mediaUrls: newList });
          this.mediaPreview = newList;
          
          // Add to our set of selected files
          this.selectedFileNames.add(file.name);
        }
      });
    }
  }

  isVideo(url: string): boolean {
    return /\.(mp4|mov|avi|webm|ogg)$/i.test(url);
  }

  isImage(url: string): boolean {
    return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
  }
  
}