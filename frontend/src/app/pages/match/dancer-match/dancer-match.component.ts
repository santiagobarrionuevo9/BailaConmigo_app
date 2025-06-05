import { Component, OnInit } from '@angular/core';
import { MatchService } from '../../../services/match.service';
import { AuthService } from '../../../services/auth.service';
import { UserContextService } from '../../../services/user-context.service';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import { MatchResponse } from '../../../models/MatchResponse';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dancer-match',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dancer-match.component.html',
  styleUrl: './dancer-match.component.css'
})
export class DancerMatchComponent implements OnInit {
  profiles: DancerProfileResponseDto[] = [];
  currentProfileIndex: number = 0;
  userId!: number;
  isLoading: boolean = false;
  error: string | null = null;
  showMatchAlert: boolean = false;
  noMoreProfiles: boolean = false;
  danceStyles: string[] = [];

  searchParams = {
    city: '',
    styles: [] as string[],
    level: '',
    availability: ''
  };
  
  constructor(
    private matchService: MatchService,
    private authService: AuthService,
    private userContext: UserContextService
  ) { }

  ngOnInit(): void {
    // Obtener ID del usuario del servicio de autenticación
    this.userId = this.userContext.userId!;
    
    // ⚡ Paso 1: Cargar estilos desde backend
    this.authService.getDanceStyles().subscribe({
      next: (styles) => {
        this.danceStyles = styles;

        // ⚡ Paso 2: Cargar perfil para obtener ciudad
        this.authService.getProfileById(this.userId).subscribe({
          next: (profile) => {
            this.searchParams.city = profile.cityName;

            // Estilos por defecto opcionalmente según su perfil
            this.searchParams.styles = Array.from(profile.danceStyles || []);

            // ✅ Finalmente, cargar perfiles
            this.loadProfiles();
          },
          error: (err) => {
            console.error('Error al obtener el perfil:', err);
            this.error = 'No se pudo cargar tu perfil.';
          }
        });
      },
      error: (err) => {
        console.error('Error al cargar estilos de baile:', err);
        this.error = 'No se pudieron cargar los estilos de baile.';
      }
    });
  }

  loadProfiles(): void {
    this.isLoading = true;
    this.error = null;
    
    this.matchService.searchDancers(
      this.userId,
      this.searchParams.city,
      this.searchParams.styles,
      this.searchParams.level,
      this.searchParams.availability
    ).subscribe({
      next: (data) => {
        this.profiles = data;
        this.currentProfileIndex = 0;
        this.isLoading = false;
        this.noMoreProfiles = this.profiles.length === 0;
      },
      error: (err) => {
        this.error = 'Error al cargar perfiles. Por favor intenta de nuevo.';
        this.isLoading = false;
        console.error('Error cargando perfiles:', err);
      }
    });
  }

  get currentProfile(): DancerProfileResponseDto | null {
    return this.profiles.length > 0 && this.currentProfileIndex < this.profiles.length 
      ? this.profiles[this.currentProfileIndex] 
      : null;
  }

    onLike(): void {
      console.log('👉 currentProfile:', this.currentProfile);
      console.log('🔍 currentProfile.id:', this.currentProfile?.userId);
      console.log('🧑 userId:', this.userId);

      if (!this.currentProfile || !this.currentProfile.userId || !this.userId) {
        console.warn('⚠️ Datos insuficientes para enviar like');
        return;
      }

      this.matchService.likeProfile(this.userId, this.currentProfile.userId).subscribe({
        next: (response: MatchResponse) => {
          if (response.matched) {
            this.showMatchAlert = true;
            setTimeout(() => this.showMatchAlert = false, 3000);
          }
          this.nextProfile();
        },
        error: (err) => {
          console.error('Error al dar like:', err);
          this.error = typeof err.error === 'string' ? err.error : 'Error al dar like. Por favor intenta de nuevo.';
          this.nextProfile();
        }
      });
    }



  onDislike(): void {
    this.nextProfile();
  }

  nextProfile(): void {
    this.currentProfileIndex++;
    
    // Verificar si hemos llegado al final de los perfiles
    if (this.currentProfileIndex >= this.profiles.length) {
      this.noMoreProfiles = true;
    }
  }

  updateSearchParams(params: any): void {
    this.searchParams = {...this.searchParams, ...params};
    this.loadProfiles();
  }

  restartSearch(): void {
    this.loadProfiles();
  }
}