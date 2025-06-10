import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-exito',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './exito.component.html',
  styleUrl: './exito.component.css'
})
export class ExitoComponent {
  constructor() {}
  
  getExpirationDate(): string {
    const expirationDateStr = localStorage.getItem('subscriptionExpiration');
    if (expirationDateStr) {
      const expirationDate = new Date(expirationDateStr);
      return expirationDate.toLocaleDateString();
    }
    return 'No disponible';
  }
}
