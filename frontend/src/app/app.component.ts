import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { LoginComponent } from "./pages/auth/login/login.component";
import { RegisterComponent } from "./pages/auth/register/register.component";
import { CommonModule } from '@angular/common';

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

  constructor(private router: Router) {}

  ngOnInit() {
    this.checkLoginStatus();
  }

  checkLoginStatus() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (token) {
      this.isLoggedIn = true;
      this.userRole = role || '';
    } else {
      this.isLoggedIn = false;
      this.userRole = '';
    }
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.isLoggedIn = false;
    this.userRole = '';
    this.router.navigate(['/login']);
  }
}
