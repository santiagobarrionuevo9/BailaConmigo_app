import { Component } from '@angular/core';
import { NavigationStart, Router, RouterLink, RouterOutlet } from '@angular/router';
import { LoginComponent } from "./pages/auth/login/login.component";
import { RegisterComponent } from "./pages/auth/register/register.component";
import { CommonModule } from '@angular/common';
import { AuthstateService } from './services/authstate.service';
import { MercadopagoService } from './services/mercadopago.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ RouterOutlet, RouterLink, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
  isLoggedIn = false;
  userRole: string = '';
  subscriptionType: string = 'BASICO';
  isProActive: boolean = false;

  constructor(
    private router: Router, 
    private authState: AuthstateService,
    private subscriptionService: MercadopagoService
  ) {}

   ngOnInit(): void {
  this.authState.isLoggedIn$.subscribe((loggedIn) => {
    this.isLoggedIn = loggedIn;
    if (loggedIn) {
      this.updateSubscriptionInfo();
    }
  });

  this.authState.userRole$.subscribe((role) => {
    this.userRole = role;
  });

  this.isLoggedIn = !!localStorage.getItem('token');
  this.userRole = localStorage.getItem('role') || '';
  this.updateSubscriptionInfo();

  this.router.events
  .pipe(filter(event => event instanceof NavigationStart))
  .subscribe((event: any) => {
    // 🔐 Solo cerrar sesión si estabas logueado
    if (event.url === '/login' && this.isLoggedIn) {
      this.logout(false); // 👈 ahora aceptará un parámetro
    }

    // Redirección automática por rol
    if (event.url === '/' || event.url === '/home') {
      const role = localStorage.getItem('role');
      if (role === 'BAILARIN') {
        this.router.navigate(['/profile']);
      } else if (role === 'ORGANIZADOR') {
        this.router.navigate(['/events']);
      }
    }
  });

}

  
  // Método para actualizar información de suscripción
  updateSubscriptionInfo(): void {
    this.subscriptionType = this.subscriptionService.getSubscriptionType();
    this.isProActive = this.subscriptionService.isProSubscriptionActive();
  }
  
  // Verificar si debe mostrar botón de actualización a PRO
  shouldShowUpgradeToPro(): boolean {
    return this.isLoggedIn && 
           this.userRole === 'BAILARIN' && 
           this.subscriptionType === 'BASICO';
  }
  
  // Verificar si debe mostrar la insignia PRO
  shouldShowProBadge(): boolean {
    return this.isLoggedIn && 
           this.userRole === 'BAILARIN' && 
           this.isProActive;
  }
  
  // Método para ir a la página de actualización a PRO
  upgradeToPro(): void {
    this.router.navigate(['/upgrade-pro']);
  }

  logout(redirectToLogin: boolean = true) {
  localStorage.clear();
  this.subscriptionService.clearSubscriptionData();
  this.authState.updateAuthState(false, '');
  this.isLoggedIn = false;
  this.userRole = '';
  this.subscriptionType = 'BASICO';
  this.isProActive = false;

  if (redirectToLogin) {
    this.router.navigate(['/login']);
  }
}


}
