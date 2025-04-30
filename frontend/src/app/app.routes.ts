import { Routes } from '@angular/router';
import { LoginComponent } from './pages/auth/login/login.component';
import { RegisterComponent } from './pages/auth/register/register.component';
import { HomeComponent } from './pages/home/home.component';
import { OrganizerEventComponent } from './pages/event/organizer-event/organizer-event.component';
import { CreateEventComponent } from './pages/event/create-event/create-event.component';
import { DancerComponent } from './pages/profile/dancer/dancer.component';

export const routes: Routes = [
    { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'profile', component: DancerComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'events', component: OrganizerEventComponent },
  { path: 'create-event', component: CreateEventComponent },
  { path: '**', redirectTo: 'home' }
  ];
