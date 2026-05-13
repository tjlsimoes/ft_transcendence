import { ApplicationConfig, APP_INITIALIZER, inject, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { catchError, firstValueFrom, of } from 'rxjs';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/services/auth.service';

/**
 * Proactively refreshes the access token at boot time when it is absent/expired
 * but the refresh token is still valid. This prevents the first-load burst of
 * 401 errors that would otherwise occur before the reactive interceptor kicks in.
 */
function initializeAuth(auth: AuthService): () => Promise<void> {
  return () => {
    const hasRefreshToken = !!auth.getRefreshToken();
    const hasAccessToken  = !!auth.getToken();

    // Nothing to do: either no session at all, or access token is already present.
    if (!hasRefreshToken || hasAccessToken) {
      return Promise.resolve();
    }

    // Refresh token exists but access token is missing → refresh proactively.
    return firstValueFrom(
      auth.refreshTokens().pipe(catchError(() => of(null)))
    ).then(() => undefined);
  };
}

// Configuração global de bootstrap da aplicação Angular.
export const appConfig: ApplicationConfig = {
  providers: [
    // Registra tratamento global de erros em runtime.
    provideBrowserGlobalErrorListeners(),
    // Habilita o roteador com as rotas definidas em app.routes.ts.
    provideRouter(routes),
    // Habilita HttpClient com interceptor de JWT.
    provideHttpClient(withInterceptors([authInterceptor])),
    // Garante access token válido antes de qualquer rota ou request disparar.
    {
      provide: APP_INITIALIZER,
      useFactory: () => {
        const auth = inject(AuthService);
        return initializeAuth(auth);
      },
      multi: true,
    },
  ]
};
