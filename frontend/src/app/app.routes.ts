import { Routes } from '@angular/router';
import { LoginComponent } from './pages/auth/login/login.component';
import { RegisterComponent } from './pages/auth/register/register.component';
import { HomeComponent } from './pages/home/home.component';
import { OrganizerEventComponent } from './pages/event/organizer-event/organizer-event.component';
import { CreateEventComponent } from './pages/event/create-event/create-event.component';
import { DancerComponent } from './pages/profile/dancer/dancer.component';
import { ForgotPasswordComponent } from './pages/auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './pages/auth/reset-password/reset-password.component';
import { MyMatchComponent } from './pages/match/my-match/my-match.component';
import { DancerMatchComponent } from './pages/match/dancer-match/dancer-match.component';
import { ChatComponent } from './pages/match/chat/chat.component';
import { ListAllEventsComponent } from './pages/profile/list-all-events/list-all-events.component';
import { EditEventComponent } from './pages/event/edit-event/edit-event.component';
import { ExitoComponent } from './pages/mercadopago/exito/exito.component';
import { RechazadoComponent } from './pages/mercadopago/rechazado/rechazado.component';
import { PendienteComponent } from './pages/mercadopago/pendiente/pendiente.component';
import { UpgradeProComponent } from './pages/mercadopago/upgrade-pro/upgrade-pro.component';
import { FaqPageComponent } from './shared/components/faq-page/faq-page.component';
import { TermsComponent } from './shared/components/terms/terms.component';
import { PrivacyComponent } from './shared/components/privacy/privacy.component';
import { OrganizerComponent } from './pages/event/organizer/organizer.component';
import { DashboardComponent } from './pages/report/dashboard/dashboard.component';
import { MatchingComponent } from './pages/report/matching/matching.component';
import { DiversityComponent } from './pages/report/diversity/diversity.component';


export const routes: Routes = [
    { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'profile', component: DancerComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'events', component: OrganizerEventComponent },
  { path: 'create-event', component: CreateEventComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'find-dancers', component: DancerMatchComponent },
  { path: 'my-matches', component: MyMatchComponent },
  { path: 'chat/:id', component: ChatComponent},
  { path: 'edit-event/:id', component: EditEventComponent },
  { path: 'all-events', component: ListAllEventsComponent },
  { path: 'pago-exito', component: ExitoComponent},
  { path: 'pago-rechazado', component: RechazadoComponent},
  { path: 'pago-pendiente', component: PendienteComponent},
  {path: 'upgrade-pro', component: UpgradeProComponent},
  { path: 'faq', component: FaqPageComponent },
  { path: 'terminos', component: TermsComponent },
  { path: 'privacidad', component: PrivacyComponent },
  {path: 'profile-organizer', component: OrganizerComponent},
  {path: 'dashboard', component: DashboardComponent},
  {path: 'reportmatching', component: MatchingComponent },
  {path: 'diversity', component: DiversityComponent}
  ];
