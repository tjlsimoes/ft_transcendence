import { Routes } from '@angular/router';
import { arenaGuard } from './core/guards/arena.guard';
import { arenaDeactivateGuard } from './core/guards/arena-deactivate.guard';
import { lobbyGuard } from './core/guards/lobby.guard';

// Mapa central de rotas da aplicação.
//
// Segurança de navegação durante duelos:
// - lobbyGuard: verifica auth + duel ativo → redireciona para /arena se IN_DUEL.
//   Aplicado a TODAS as rotas protegidas fora da arena.
// - arenaGuard: verifica auth + existência de duel ativo no backend.
//   O URL é apenas /arena — sem query params. Dados vêm do backend.
// - arenaDeactivateGuard: bloqueia TODA a navegação fora da arena durante duel ativo.
export const routes: Routes = [
  // Landing page — pública.
  {
    path: '',
    loadComponent: () => import('./features/home/pages/home/home').then(m => m.Home),
  },
  // Tela de autenticação — pública.
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login').then(m => m.LoginComponent),
  },
  // Tela de criação de conta — pública.
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register').then(m => m.RegisterComponent),
  },
  // OAuth callback route — pública.
  {
    path: 'oauth2/callback',
    loadComponent: () => import('./auth/oauth-callback/oauth-callback').then(m => m.OAuthCallbackComponent),
  },
  // About — pública.
  {
    path: 'about',
    loadComponent: () => import('./features/about/about').then(m => m.About),
  },
  // Lobby — requer autenticação + sem duel ativo.
  {
    path: 'lobby',
    canActivate: [lobbyGuard],
    loadComponent: () => import('./features/dashboard/lobby/lobby').then(m => m.Lobby),
  },
  // Perfil — requer autenticação + sem duel ativo.
  {
    path: 'profile',
    canActivate: [lobbyGuard],
    loadComponent: () => import('./features/dashboard/profile-settings/profile-settings').then(m => m.ProfileSettings),
  },
  // Arena — requer autenticação + duel ativo no backend.
  // Sem query params — toda a informação vem do backend.
  {
    path: 'arena',
    canActivate: [arenaGuard],
    canDeactivate: [arenaDeactivateGuard],
    loadComponent: () => import('./arena-page/arena-page').then(m => m.ArenaPage),
  },
  // Leaderboard — requer autenticação + sem duel ativo.
  {
    path: 'leaderboard',
    canActivate: [lobbyGuard],
    loadComponent: () => import('./features/ranking/ranking').then(m => m.Ranking),
  },
  // Fallback.
  { path: '**', redirectTo: '' },
];
