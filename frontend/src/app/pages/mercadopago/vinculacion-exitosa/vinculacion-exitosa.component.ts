import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-vinculacion-exitosa',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './vinculacion-exitosa.component.html',
  styleUrl: './vinculacion-exitosa.component.css'
})
export class VinculacionExitosaComponent implements OnInit {

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Opcional: mostrar un mensaje de confirmación por unos segundos
    // y luego redirigir automáticamente
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goToHome(): void {
    this.router.navigate(['/']);
  }
}
