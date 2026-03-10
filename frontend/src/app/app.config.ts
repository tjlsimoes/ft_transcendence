import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';

// Configuração global de bootstrap da aplicação Angular.
export const appConfig: ApplicationConfig = {
  providers: [
    // Registra tratamento global de erros em runtime.
    provideBrowserGlobalErrorListeners(),
    // Habilita o roteador com as rotas definidas em app.routes.ts.
    provideRouter(routes),
    // Habilita HttpClient para chamadas HTTP ao backend.
    provideHttpClient(),
  ]
};
