import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Role } from '../../../models/role';
import { SubscriptionType } from '../../../models/subscription-type';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';
import { RegisterRequestDto } from '../../../models/register-request';
import { Router, RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { Country } from '../../../models/Country';
import { City } from '../../../models/City';
import { LocationService } from '../../../services/location.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  roles = Object.values(Role);
  subscriptionTypes = Object.values(SubscriptionType);
  countries: Country[] = [];
  cities: City[] = [];
  errorMessage: string = '';
  successMessage: string = '';
  isOrganizerAndAcademy: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private locationService: LocationService
  ) {
    this.registerForm = this.fb.group({
      role: ['', Validators.required],
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      gender: ['', Validators.required],
      birthdate: ['', Validators.required],
      countryId: ['', Validators.required],
      cityId: ['', Validators.required],
      subscriptionType: ['', Validators.required],
      isAcademy: [false]
    });

    this.registerForm.get('role')?.valueChanges.subscribe((role) => {
      this.updateFieldsForRole(role);
    });

    this.registerForm.get('isAcademy')?.valueChanges.subscribe((isAcademy) => {
      if (this.registerForm.get('role')?.value === 'ORGANIZADOR') {
        this.setOrganizerAcademyDefaults(isAcademy);
      }
    });
  }

  ngOnInit(): void {
    this.loadCountries();

    // Setear y deshabilitar permanentemente la suscripción
    this.registerForm.get('subscriptionType')?.setValue('BASICO');
    this.registerForm.get('subscriptionType')?.disable();
  }


  loadCountries(): void {
    this.locationService.getAllCountries().subscribe({
      next: (data) => this.countries = data,
      error: () => Swal.fire('Error', 'No se pudieron cargar los países.', 'error')
    });
  }

  onCountryChange(): void {
    const selectedCountryId = this.registerForm.get('countryId')?.value;
    if (selectedCountryId) {
      this.locationService.getCitiesByCountry(selectedCountryId).subscribe({
        next: (data) => this.cities = data,
        error: () => Swal.fire('Error', 'No se pudieron cargar las ciudades.', 'error')
      });
    } else {
      this.cities = [];
      this.registerForm.get('cityId')?.setValue('');
    }
  }

  updateFieldsForRole(role: string): void {
    if (role === 'ORGANIZADOR') {
      this.registerForm.get('subscriptionType')?.setValue('BASICO');
      this.registerForm.get('subscriptionType')?.disable();
    } else {
      this.registerForm.get('subscriptionType')?.enable();
      this.isOrganizerAndAcademy = false; // Reset cuando no es organizador
    }

    const isAcademy = this.registerForm.get('isAcademy')?.value;
    this.setOrganizerAcademyDefaults(isAcademy);
  }

  setOrganizerAcademyDefaults(isAcademy: boolean): void {
    if (this.registerForm.get('role')?.value !== 'ORGANIZADOR') return;

    if (isAcademy) {
      const formattedDate = '2000-06-11';

      this.registerForm.patchValue({
        birthdate: formattedDate,
        gender: 'OTRO'
      });
    } else {
      // Para organizadores individuales, limpiar los campos para que puedan elegir
      this.registerForm.patchValue({
        birthdate: '',
        gender: ''
      });
    }
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    const registerRequest: RegisterRequestDto = {
      ...this.registerForm.getRawValue(), // Incluye los deshabilitados como subscriptionType
    };

    this.authService.register(registerRequest).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: '¡Registro exitoso!',
          text: 'Tu cuenta fue creada correctamente.',
          confirmButtonText: 'Ir al login',
          confirmButtonColor: '#b34700'
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

  onRoleChange(): void {
    const role = this.registerForm.get('role')?.value;
    if (role !== 'ORGANIZADOR') {
      this.isOrganizerAndAcademy = false;
      this.registerForm.get('subscriptionType')?.enable();
      this.registerForm.get('birthdate')?.enable();
      this.registerForm.get('gender')?.enable();
    }
  }

  onAcademyToggle(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.isOrganizerAndAcademy = checked;

    if (checked && this.registerForm.get('role')?.value === 'ORGANIZADOR') {
      // Para academias: setear valores automáticos
      const eighteenYearsAgo = new Date();
      eighteenYearsAgo.setFullYear(eighteenYearsAgo.getFullYear() - 18);
      const formattedDate = eighteenYearsAgo.toISOString().split('T')[0];

      this.registerForm.patchValue({
        subscriptionType: 'BASICO',
        birthdate: formattedDate,
        gender: 'OTRO'
      });

      // Deshabilitar campos que se setean automáticamente
      this.registerForm.get('subscriptionType')?.disable();
      this.registerForm.get('birthdate')?.disable();
      this.registerForm.get('gender')?.disable();
    } else {
      // Cuando se desmarca academia, habilitar los campos
      this.registerForm.get('subscriptionType')?.enable();
      this.registerForm.get('birthdate')?.enable();
      this.registerForm.get('gender')?.enable();
      
      // Limpiar los valores para que el usuario pueda elegir
      this.registerForm.patchValue({
        birthdate: '',
        gender: ''
      });
    }
  }
}