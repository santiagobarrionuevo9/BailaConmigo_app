import { Component } from '@angular/core';

@Component({
  selector: 'app-exito',
  standalone: true,
  imports: [],
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
