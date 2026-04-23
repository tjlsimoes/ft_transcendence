import { Routes } from '@angular/router';

// Mapa central de rotas públicas da aplicação.
// Todas as rotas usam lazy loading para reduzir o bundle inicial.
export const routes: Routes = [
  // Landing page.
  {
    path: '',
    loadComponent: () => import('./features/home/pages/home/home').then(m => m.Home),
  },
  // Tela de autenticação de entrada.
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login').then(m => m.LoginComponent),
  },
  // Tela de criação de conta.
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register').then(m => m.RegisterComponent),
  },
  // Pagina de about.
  {
    path: 'about',
    loadComponent: () => import('./features/about/about').then(m => m.About),
  },
  // Lobby do dashboard.
  {
    path: 'lobby',
    loadComponent: () => import('./features/dashboard/lobby/lobby').then(m => m.Lobby),
  },
  // Configurações de perfil do jogador.
  {
    path: 'profile',
    loadComponent: () => import('./features/dashboard/profile-settings/profile-settings').then(m => m.ProfileSettings),
  },
  // Ranking / leaderboard.
  {
    path: 'ranking',
    loadComponent: () => import('./features/ranking/ranking').then(m => m.Ranking),
  },
  // Fallback: rota desconhecida redireciona para home.
  { path: '**', redirectTo: '' },
];
