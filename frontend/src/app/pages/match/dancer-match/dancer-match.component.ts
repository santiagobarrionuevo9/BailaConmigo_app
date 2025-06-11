import { Component, OnInit } from '@angular/core';
import { MatchService } from '../../../services/match.service';
import { AuthService } from '../../../services/auth.service';
import { UserContextService } from '../../../services/user-context.service';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import { MatchResponse } from '../../../models/MatchResponse';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Level } from '../../../models/level';

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
  subscriptionType: string | null = null;
  
  // Niveles disponibles del enum
  levels = Object.values(Level);

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
    this.userId = this.userContext.userId!;
    this.subscriptionType = this.userContext.SubscriptionType;
    
    // Cargar estilos de baile disponibles
    this.authService.getDanceStyles().subscribe({
      next: (styles) => {
        this.danceStyles = styles;
        
        if (this.subscriptionType === 'BASIC') {
          // Para usuarios básicos, cargar automáticamente
          this.loadProfiles();
        } else {
          // Para usuarios PRO, cargar perfil para valores por defecto
          this.loadUserProfileDefaults();
        }
      },
      error: (err) => {
        console.error('Error al cargar estilos de baile:', err);
        this.error = 'No se pudieron cargar los estilos de baile.';
      }
    });
  }

  /**
   * Carga valores por defecto del perfil del usuario para usuarios PRO
   */
  private loadUserProfileDefaults(): void {
    this.authService.getProfileById(this.userId).subscribe({
      next: (profile) => {
        this.searchParams.city = profile.cityName;
        this.searchParams.level = profile.level;
        this.searchParams.styles = Array.from(profile.danceStyles || []);
        
        // Cargar perfiles con valores por defecto
        this.loadProfiles();
      },
      error: (err) => {
        console.error('Error al obtener el perfil:', err);
        this.error = 'No se pudo cargar tu perfil.';
      }
    });
  }

  loadProfiles(): void {
    this.isLoading = true;
    this.error = null;
    
    if (this.subscriptionType === 'BASIC') {
      // Para usuarios básicos, el servicio maneja automáticamente los parámetros
      this.matchService.searchDancers(this.userId).subscribe({
        next: (data) => this.handleProfilesResponse(data),
        error: (err) => this.handleProfilesError(err)
      });
    } else {
      // Para usuarios PRO, enviar parámetros personalizados
      this.matchService.searchDancers(
        this.userId,
        this.searchParams.city,
        this.searchParams.styles,
        this.searchParams.level,
        this.searchParams.availability
      ).subscribe({
        next: (data) => this.handleProfilesResponse(data),
        error: (err) => this.handleProfilesError(err)
      });
    }
  }

  private handleProfilesResponse(data: DancerProfileResponseDto[]): void {
    this.profiles = data;
    this.currentProfileIndex = 0;
    this.isLoading = false;
    this.noMoreProfiles = this.profiles.length === 0;
  }

  private handleProfilesError(err: any): void {
    this.error = 'Error al cargar perfiles. Por favor intenta de nuevo.';
    this.isLoading = false;
    console.error('Error cargando perfiles:', err);
  }

  get currentProfile(): DancerProfileResponseDto | null {
    return this.profiles.length > 0 && this.currentProfileIndex < this.profiles.length 
      ? this.profiles[this.currentProfileIndex] 
      : null;
  }

  get isProUser(): boolean {
    return this.subscriptionType === 'PRO';
  }

  get isBasicUser(): boolean {
    return this.subscriptionType === 'BASIC';
  }

  onLike(): void {
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
    
    if (this.currentProfileIndex >= this.profiles.length) {
      this.noMoreProfiles = true;
    }
  }

  /**
   * Solo disponible para usuarios PRO
   */
  updateSearchParams(params: any): void {
    if (this.isProUser) {
      this.searchParams = {...this.searchParams, ...params};
      this.loadProfiles();
    }
  }

  restartSearch(): void {
    this.loadProfiles();
  }

  /**
   * Maneja el cambio de estilos de baile (solo para usuarios PRO)
   */
  onStyleChange(style: string, event: any): void {
    if (!this.isProUser) return;
    
    if (event.target.checked) {
      if (!this.searchParams.styles.includes(style)) {
        this.searchParams.styles.push(style);
      }
    } else {
      this.searchParams.styles = this.searchParams.styles.filter(s => s !== style);
    }
  }

  /**
   * Verifica si un estilo está seleccionado
   */
  isStyleSelected(style: string): boolean {
    return this.searchParams.styles.includes(style);
  }
}