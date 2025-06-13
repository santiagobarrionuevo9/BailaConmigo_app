import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-rating-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rating-modal.component.html',
  styleUrl: './rating-modal.component.css'
})
export class RatingModalComponent {
  @Input() visible = false;
  @Input() profileId!: number;
  @Input() raterId!: number;
  @Output() close = new EventEmitter();
  @Output() submit = new EventEmitter<{ stars: number, comment: string }>();

  stars = 0;
  alreadyRated = false;

  setStars(value: number) {
    if (!this.alreadyRated) {
      this.stars = value;
    }
  }

  sendRating() {
    if (!this.alreadyRated && this.stars > 0) {
      this.submit.emit({ stars: this.stars, comment: '' });
    }
  }

  handleAlreadyRated() {
    this.alreadyRated = true;
    this.stars = 0;
  }

  reset() {
    this.stars = 0;
    this.alreadyRated = false;
    this.close.emit();
  }
}