import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LoginRequestDto } from '../../../models/login-request';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    const loginRequest = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    };

    this.authService.login(loginRequest).subscribe({
      next: (response) => {
        // Guardamos token y datos del usuario
        localStorage.setItem('token', response.token);
        localStorage.setItem('userId', response.userId.toString());
        localStorage.setItem('fullName', response.fullName);
        localStorage.setItem('role', response.role);

        // Redirigir o hacer algo después de login exitoso
        console.log('Login exitoso', response);
      },
      error: (error) => {
        console.error('Error al iniciar sesión', error);
        this.errorMessage = 'Credenciales incorrectas.';
      }
    });
  }
}
