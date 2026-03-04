import { Routes } from '@angular/router';
import { Home } from './features/home/pages/home/home';
import { LoginComponent } from './auth/login/login';
import { RegisterComponent } from './auth/register/register';

// Mapa central de rotas públicas da aplicação.
export const routes: Routes = [
  // Landing page.
  { path: '', component: Home },
  // Tela de autenticação de entrada.
  { path: 'login', component: LoginComponent },
  // Tela de criação de conta.
  { path: 'register', component: RegisterComponent },
  // Fallback: rota desconhecida redireciona para home.
  { path: '**', redirectTo: '' },
];
