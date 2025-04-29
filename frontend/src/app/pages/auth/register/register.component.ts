import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Role } from '../../../models/role';
import { SubscriptionType } from '../../../models/subscription-type';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RegisterRequestDto } from '../../../models/register-request';
import { RouterLink } from '@angular/router';

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
    private authService: AuthService
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

    const registerRequest = {
      fullName: this.registerForm.value.fullName,
      email: this.registerForm.value.email,
      password: this.registerForm.value.password,
      gender: this.registerForm.value.gender,
      birthdate: this.registerForm.value.birthdate,
      city: this.registerForm.value.city,
      role: this.registerForm.value.role,
      subscriptionType: this.registerForm.value.subscriptionType
    };

    this.authService.register(this.registerForm.value).subscribe({
      next: (response) => {
        this.successMessage = response;
        this.errorMessage = '';
        this.registerForm.reset();
      },
      error: (error) => {
        this.errorMessage = error.error || 'Error en el registro.';
        this.successMessage = '';
      }
    });
    
  }
}
