import { Component, OnInit } from '@angular/core';
import { UserContextService } from '../../../services/user-context.service';
import { MercadopagoService } from '../../../services/mercadopago.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-upgrade-pro',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upgrade-pro.component.html',
  styleUrl: './upgrade-pro.component.css'
})
export class UpgradeProComponent implements OnInit {
  isLoading: boolean = false;
  errorMessage: string = '';
  
  constructor(
    private subscriptionService: MercadopagoService,
    private userContext: UserContextService
  ) {}
  
  ngOnInit(): void {
    // Verificar si el usuario ya tiene suscripción PRO
    if (this.subscriptionService.getSubscriptionType() === 'PRO' && 
        this.subscriptionService.isProSubscriptionActive()) {
      this.errorMessage = 'Ya tienes una membresía PRO activa.';
    }
  }
  
  initiateProSubscription(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    const email = this.userContext.Email;
    
    if (email) {
      this.subscriptionService.generateProSubscriptionLink(email)
      .subscribe({
        next: (response) => {
        // Redireccionar al usuario al link de pago de MercadoPago
        window.location.href = response.init_point;
        },
        error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Ocurrió un error al procesar tu solicitud. Por favor intenta de nuevo más tarde.';
        console.error('Error al generar link de pago:', error);
        }
      });
    } else {
      this.isLoading = false;
      this.errorMessage = 'El correo electrónico del usuario no está disponible.';
    }
  }
}
