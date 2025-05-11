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
  @Output() close = new EventEmitter<void>();
  @Output() submit = new EventEmitter<{ stars: number, comment: string }>();

  stars = 0;
  comment = '';

  setStars(value: number) {
    this.stars = value;
  }

  sendRating() {
    this.submit.emit({ stars: this.stars, comment: this.comment });
    this.reset();
  }

  reset() {
    this.stars = 0;
    this.comment = '';
    this.close.emit();
  }
}