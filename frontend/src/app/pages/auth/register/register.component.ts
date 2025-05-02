import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Role } from '../../../models/role';
import { SubscriptionType } from '../../../models/subscription-type';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RegisterRequestDto } from '../../../models/register-request';
import { Router, RouterLink } from '@angular/router';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  roles = Object.values(Role);
  subscriptionTypes = Object.values(SubscriptionType);
  errorMessage: string = '';
  successMessage: string = '';


  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      gender: ['', Validators.required],
      birthdate: ['', Validators.required],
      city: ['', Validators.required],
      role: ['', Validators.required],
      subscriptionType: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;
  
    const registerRequest = this.registerForm.value;
  
    this.authService.register(registerRequest).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: '¡Registro exitoso!',
          text: 'Tu cuenta fue creada correctamente.',
          confirmButtonText: 'Ir al login'
        }).then(() => {
          this.router.navigate(['/login']);
        });
      },
      error: (error) => {
        Swal.fire({
          icon: 'error',
          title: 'Error al registrarse',
          text: error.error || 'Ocurrió un error durante el registro.',
          confirmButtonText: 'Volver al login'
        }).then(() => {
          this.router.navigate(['/login']);
        });
      }
    });
  }
  
}
