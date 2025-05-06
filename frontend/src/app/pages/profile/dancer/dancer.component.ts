import { Component, OnInit } from '@angular/core';
import {  FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ProfileService } from '../../../services/profile.service';
import { EditDancerProfileDto } from '../../../models/editdancerprofile';
import { CommonModule } from '@angular/common';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import Swal from 'sweetalert2';
import { UserContextService } from '../../../services/user-context.service';
import { AuthstateService } from '../../../services/authstate.service';
import { AuthService } from '../../../services/auth.service';

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
  
  availableDanceStyles: string[] = [];
  selectedStyles: string[] = [];

  constructor(
    private fb: FormBuilder, 
    private profileService: ProfileService,
    private userContext: UserContextService, 
    private auhtservice: AuthService
  ) {}

  ngOnInit(): void {
    this.userId = this.userContext.userId!;
    this.form = this.fb.group({
      city: [''],
      danceStyles: [[]],
      level: [''],
      aboutMe: [''],
      availability: [''],
      mediaUrls: [[]]
    });
    
    this.auhtservice.getDanceStyles().subscribe(styles => {
      this.availableDanceStyles = styles;
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
  
  // Métodos para el carrusel tipo Tinder
  nextMedia(): void {
    if (this.mediaPreview.length === 0) return;
    this.currentMediaIndex = (this.currentMediaIndex + 1) % this.mediaPreview.length;
  }
  
  previousMedia(): void {
    if (this.mediaPreview.length === 0) return;
    this.currentMediaIndex = (this.currentMediaIndex - 1 + this.mediaPreview.length) % this.mediaPreview.length;
  }
  
  // Método para eliminar media en modo edición
  removeMedia(index: number): void {
    // Extraer el nombre del archivo para eliminarlo de nuestro Set
    const url = this.mediaPreview[index];
    const fileName = this.extractFileNameFromUrl(url);
    if (fileName) {
      this.selectedFileNames.delete(fileName);
    }
    
    // Eliminar de la vista previa y actualizar el formulario
    this.mediaPreview.splice(index, 1);
    this.form.patchValue({ mediaUrls: [...this.mediaPreview] });
    
    // Resetear el índice si era el último elemento
    if (this.currentMediaIndex >= this.mediaPreview.length) {
      this.currentMediaIndex = Math.max(0, this.mediaPreview.length - 1);
    }
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

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      // Check if this file has already been selected based on name
      if (this.selectedFileNames.has(file.name)) {
        Swal.fire({
          icon: 'warning',
          title: 'Archivo duplicado',
          text: 'Ya has seleccionado este archivo anteriormente.',
          confirmButtonColor: '#ff6600',
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