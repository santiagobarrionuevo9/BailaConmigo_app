import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule, NgModel, ReactiveFormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-cancelevent',
  standalone: true,
  imports: [CommonModule, FormsModule ],
  templateUrl: './cancelevent.component.html',
  styleUrl: './cancelevent.component.css'
})
export class CanceleventComponent {

  @Input() visible = false;
  @Output() cancel = new EventEmitter<string>();
  @Output() close = new EventEmitter<void>();

  reason = '';

  async onSubmit() {
    if (!this.reason.trim()) return;

    const result = await Swal.fire({
      title: '¿Estás seguro?',
      text: 'Esta acción cancelará el evento y notificará a los asistentes.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No',
      confirmButtonColor: '#cc5500',
      cancelButtonColor: '#6c757d'
    });

    if (result.isConfirmed) {
      this.cancel.emit(this.reason.trim());
      this.reason = '';
    }
  }

  onClose() {
    this.reason = '';
    this.close.emit();
  }
}