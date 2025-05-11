import { Component, OnInit } from '@angular/core';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import { MatchService } from '../../../services/match.service';
import { UserContextService } from '../../../services/user-context.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ChatService } from '../../../services/chat.service';

@Component({
  selector: 'app-my-match',
  standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './my-match.component.html',
  styleUrl: './my-match.component.css'
})
export class MyMatchComponent implements OnInit {
  matches: DancerProfileResponseDto[] = [];
  isLoading: boolean = false;
  error: string | null = null;
  userId!: number;
  activeRecipientId: number | null = null;
  messageContent: string = '';

  constructor(
    private matchService: MatchService,
    private contextService: UserContextService,
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
    this.userId = this.contextService.userId!;
    // Cargar los matches al iniciar el componente);
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

  
}
