import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import { ChatMessageDto } from '../../../models/ChatMessageDto';
import { Subscription } from 'rxjs/internal/Subscription';
import { Subject } from 'rxjs/internal/Subject';
import { ChatService } from '../../../services/chat.service';
import { UserContextService } from '../../../services/user-context.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { interval } from 'rxjs/internal/observable/interval';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule,FormsModule,RouterLink],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  
  currentUserId!: number;
  matchUserId!: number;
  matchProfile: DancerProfileResponseDto | null = null;
  messages: ChatMessageDto[] = [];
  newMessage: string = '';
  isLoading: boolean = true;
  error: string | null = null;
  
  private destroy$ = new Subject<void>();
  private messagePolling!: Subscription;
  
  constructor(
    private chatService: ChatService,
    private userContextService: UserContextService,
    private dancerProfileService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.userContextService.userId!;
    
    this.route.params.subscribe(params => {
      this.matchUserId = +params['id']; // Convertir a número
      this.loadMatchProfile();
      this.loadChatHistory();
      
      // Configurar polling para actualizar mensajes cada 5 segundos
      this.messagePolling = interval(5000)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.loadChatHistory(false);
        });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.messagePolling) {
      this.messagePolling.unsubscribe();
    }
  }

  loadMatchProfile(): void {
    this.dancerProfileService.getProfileById(this.matchUserId).subscribe({
      next: (profile) => {
        this.matchProfile = profile;
      },
      error: (err) => {
        console.error('Error cargando perfil del match:', err);
        this.error = 'No se pudo cargar el perfil del match';
      }
    });
  }

  loadChatHistory(showLoading: boolean = true): void {
    if (showLoading) {
      this.isLoading = true;
    }
    
    this.chatService.getChatHistory(this.currentUserId, this.matchUserId).subscribe({
      next: (data) => {
        this.messages = data;
        this.isLoading = false;
        // Scroll al final después de cargar mensajes
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (err) => {
        console.error('Error cargando historial de chat:', err);
        this.error = 'Error al cargar los mensajes. Por favor intenta de nuevo.';
        this.isLoading = false;
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim()) return;
    
    const messageDto = {
      senderId: this.currentUserId,
      recipientId: this.matchUserId,
      content: this.newMessage.trim()
    };
    
    // Optimistic update - add message to UI immediately
    const optimisticMessage: ChatMessageDto = {
      senderId: this.currentUserId,
      senderName: this.userContextService.fullName || 'Tú',
      recipientId: this.matchUserId,
      recipientName: this.matchProfile?.fullName || 'Match',
      content: this.newMessage.trim(),
      timestamp: new Date()
    };
    
    this.messages.push(optimisticMessage);
    this.newMessage = '';
    this.scrollToBottom();
    
    // Send message to server
    this.chatService.sendMessage(messageDto).subscribe({
      next: () => {
        // Message sent successfully
        this.loadChatHistory(false);
      },
      error: (err) => {
        console.error('Error enviando mensaje:', err);
        // Remove the optimistic message on error
        this.messages.pop();
        this.error = 'No se pudo enviar el mensaje. Por favor intenta de nuevo.';
      }
    });
  }

    onEnterPress(event: Event): void {
    const keyboardEvent = event as KeyboardEvent;
    if (keyboardEvent.key === 'Enter' && !keyboardEvent.shiftKey) {
      keyboardEvent.preventDefault();
      this.sendMessage();
    }
  }


  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = 
        this.messagesContainer.nativeElement.scrollHeight;
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }
}
