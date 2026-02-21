import { Routes } from '@angular/router';
import { Home } from './home/home';
import { Stats } from './stats/stats';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { Dashboard } from './dashboard/dashboard';
import { Analytics } from './analytics/analytics/analytics';
import { Unlock } from './unlock/unlock';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'home', redirectTo: '', pathMatch: 'full' },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  { path: 'unlock', component: Unlock },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'analytics', component: Analytics, canActivate: [authGuard] },
  { path: 'stats/:shortCode', component: Stats, canActivate: [authGuard] }
];
