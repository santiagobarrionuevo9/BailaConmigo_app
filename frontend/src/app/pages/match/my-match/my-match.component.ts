import { Component, OnInit } from '@angular/core';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import { MatchService } from '../../../services/match.service';
import { UserContextService } from '../../../services/user-context.service';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ChatService } from '../../../services/chat.service';
import { MatchprofileModalComponent } from "../matchprofile-modal/matchprofile-modal.component";

@Component({
  selector: 'app-my-match',
  standalone: true,
  imports: [CommonModule, RouterLink, MatchprofileModalComponent],
  templateUrl: './my-match.component.html',
  styleUrl: './my-match.component.css'
})
export class MyMatchComponent implements OnInit {
  matches: DancerProfileResponseDto[] = [];
  isLoading: boolean = false;
  error: string | null = null;
  userId!: number;

  selectedMatch?: DancerProfileResponseDto;
  showProfileModal: boolean = false;

  constructor(
    private matchService: MatchService,
    private contextService: UserContextService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId = this.contextService.userId!;
    this.loadMatches();
  }

  loadMatches(): void {
    this.isLoading = true;
    this.error = null;

    this.matchService.getMatches(this.userId).subscribe({
      next: (data) => {
        this.matches = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar tus matches. Por favor intenta de nuevo.';
        this.isLoading = false;
        console.error('Error cargando matches:', err);
      }
    });
  }

  navigateToChat(matchUserId: number): void {
    if (matchUserId != null) {
      this.router.navigate(['/chat', matchUserId]);
    } else {
      console.error('Error: matchUserId es null o undefined. No se puede navegar.');
    }
  }

  openProfileModal(match: DancerProfileResponseDto): void {
    this.selectedMatch = match;
    this.showProfileModal = true;
  }

  closeProfileModal(): void {
    this.showProfileModal = false;
  }

}
