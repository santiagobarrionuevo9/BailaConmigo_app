import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import Swal from 'sweetalert2';
// Validador personalizado para verificar que las contraseñas coincidan
export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const newPassword = control.get('newPassword');
  const confirmPassword = control.get('confirmPassword');

  return newPassword && confirmPassword && newPassword.value !== confirmPassword.value 
    ? { passwordsNotMatch: true } 
    : null;
};
@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm: FormGroup;
  token: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  tokenValidado: boolean = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    this.resetPasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: passwordMatchValidator });
  }

  ngOnInit(): void {
    // Obtener el token desde los parámetros de la URL
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      
      if (!this.token) {
        this.errorMessage = 'El enlace no es válido o ha expirado. Por favor, solicita un nuevo correo de recuperación.';
        return;
      }

      // Opcionalmente: validar el token en el backend antes de mostrar el formulario
      this.tokenValidado = true; // Por ahora asumimos que es válido
      
      // Si quieres verificar el token con el backend primero:
      /* 
      this.authService.validateToken(this.token).subscribe({
        next: (response) => {
          this.tokenValidado = true;
        },
        error: (error) => {
          this.errorMessage = 'El enlace ha expirado o no es válido. Por favor, solicita un nuevo correo de recuperación.';
          this.tokenValidado = false;
        }
      });
      */
    });
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid || !this.token) {
      return;
    }
  
    const resetPasswordRequest = {
      token: this.token,
      newPassword: this.resetPasswordForm.value.newPassword
    };
  
    this.authService.resetPassword(resetPasswordRequest).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Contraseña actualizada',
          text: 'Tu contraseña ha sido restablecida correctamente.',
          confirmButtonText: 'Ir al login'
        }).then(() => {
          this.router.navigate(['/login']);
        });
      },
      error: (error) => {
        console.error('Error al restablecer la contraseña', error);
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'El enlace ha expirado o no es válido. Por favor, solicita un nuevo correo de recuperación.',
          confirmButtonText: 'Aceptar'
        });
      }
    });
  }
  
}
