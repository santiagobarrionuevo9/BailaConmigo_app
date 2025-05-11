import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-matchprofile-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './matchprofile-modal.component.html',
  styleUrl: './matchprofile-modal.component.css'
})
export class MatchprofileModalComponent {
  @Input() dancer!: DancerProfileResponseDto;
  @Input() currentUserId!: number;
  @Output() close = new EventEmitter<void>();

  ratingStars: number = 0;
  comment: string = '';
  submitted: boolean = false;

  constructor(private authService: AuthService) {}

  setRating(stars: number) {
    this.ratingStars = stars;
  }

  submitRating() {
    if (!this.ratingStars || !this.comment.trim()) return;

    const ratingData = {
      profileId: this.dancer.userId,
      stars: this.ratingStars,
      comment: this.comment
    };

    this.authService.rateProfile(this.currentUserId, ratingData).subscribe({
      next: () => {
        this.submitted = true;
      },
      error: err => console.error('Error enviando rating:', err)
    });
  }

  closeModal() {
    this.close.emit();
  }
}
