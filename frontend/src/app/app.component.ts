import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { LoginComponent } from "./pages/auth/login/login.component";
import { RegisterComponent } from "./pages/auth/register/register.component";
import { CommonModule } from '@angular/common';
import { AuthstateService } from './services/authstate.service';

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

  constructor(private router: Router, private authState: AuthstateService ) {}

  ngOnInit(): void {
    this.authState.isLoggedIn$.subscribe((loggedIn) => {
      this.isLoggedIn = loggedIn;
    });

    this.authState.userRole$.subscribe((role) => {
      this.userRole = role;
    });
    const role = localStorage.getItem('role'); // o traerlo del token
    if (role === 'BAILARIN') {
      this.router.navigate(['/profile']);
    } else if (role === 'ORGANIZADOR') {
      this.router.navigate(['/events']);
    }
  }
  

  

  logout() {
    localStorage.clear();
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.authState.updateAuthState(false, '');
    this.isLoggedIn = false;
    this.userRole = '';
    this.router.navigate(['/home']);
  }
}
