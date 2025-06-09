import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-vinculacion-error',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './vinculacion-error.component.html',
  styleUrl: './vinculacion-error.component.css'
})
export class VinculacionErrorComponent implements OnInit {

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Opcional: logging del error
    console.log('Error en vinculación con MercadoPago');
  }

  tryAgain(): void {
    this.router.navigate(['/vinculacion']);
  }

  goToHome(): void {
    this.router.navigate(['/']);
  }
}