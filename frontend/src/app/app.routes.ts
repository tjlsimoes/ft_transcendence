import { Routes } from '@angular/router';
import { Home } from './features/home/pages/home/home';
import { LoginComponent } from './auth/login/login';
import { RegisterComponent } from './auth/register/register';
import { About } from './about/about';
import { Lobby } from './features/dashboard/lobby/lobby';

// Mapa central de rotas públicas da aplicação.
export const routes: Routes = [
  // Landing page.
  { path: '', component: Home },
  // Tela de autenticação de entrada.
  { path: 'login', component: LoginComponent },
  // Tela de criação de conta.
  { path: 'register', component: RegisterComponent },
  //pagina de about.
  { path: 'about', component: About },
  // Lobby do dashboard.
  { path: 'lobby', component: Lobby },
  // Fallback: rota desconhecida redireciona para home.
  { path: '**', redirectTo: '' },
];
