import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

// Configuração global de bootstrap da aplicação Angular.
export const appConfig: ApplicationConfig = {
  providers: [
    // Registra tratamento global de erros em runtime.
    provideBrowserGlobalErrorListeners(),
    // Habilita o roteador com as rotas definidas em app.routes.ts.
    provideRouter(routes),
    // Habilita HttpClient com interceptor de JWT.
    provideHttpClient(withInterceptors([authInterceptor])),
  ]
};
