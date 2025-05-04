import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) {
      return;
    }

    const forgotPasswordRequest = {
      email: this.forgotPasswordForm.value.email
    };

    this.authService.forgotPassword(forgotPasswordRequest).subscribe({
      next: (response) => {
        this.successMessage = 'Se ha enviado un correo con instrucciones para restablecer tu contraseña';
        this.errorMessage = '';
        // Opcional: Redirigir después de unos segundos
        // setTimeout(() => {
        //   this.router.navigate(['/login']);
        // }, 3000);
      },
      error: (error) => {
        console.error('Error al solicitar recuperación de contraseña', error);
        // Mostrar un mensaje genérico para no revelar si el email existe o no
        this.successMessage = 'Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña';
        this.errorMessage = '';
      }
    });
  }
}
