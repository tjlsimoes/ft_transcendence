import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

// Mapa central de rotas públicas da aplicação.
// Todas as rotas usam lazy loading para reduzir o bundle inicial.
export const routes: Routes = [
  // Landing page — redireciona para /lobby se já estiver logado.
  {
    path: '',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/home/pages/home/home').then(m => m.Home),
  },
  // Tela de autenticação de entrada — inacessível se já logado.
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./auth/login/login').then(m => m.LoginComponent),
  },
  // Tela de criação de conta — inacessível se já logado.
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./auth/register/register').then(m => m.RegisterComponent),
  },
  // Pagina de about.
  {
    path: 'about',
    loadComponent: () => import('./features/about/about').then(m => m.About),
  },
  // Lobby do dashboard — requer autenticação.
  {
    path: 'lobby',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/lobby/lobby').then(m => m.Lobby),
  },
  // Configurações de perfil do jogador — requer autenticação.
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/profile-settings/profile-settings').then(m => m.ProfileSettings),
  },
  // Leaderboard (ranking global de jogadores).
  {
    path: 'leaderboard',
    loadComponent: () => import('./features/ranking/ranking').then(m => m.Ranking),
  },
  // Fallback: rota desconhecida redireciona para home.
  { path: '**', redirectTo: '' },
];
