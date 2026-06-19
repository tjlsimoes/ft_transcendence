import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  // Don't send tokens to login/register — they don't need them,
  // and a stale token would cause unnecessary JWT validation.
  // Logout DOES need the token so the backend can identify the user.
  // We also exclude logout from 401 handling to avoid infinite loops.
  const isPublicAuthEndpoint =
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/auth/register') ||
    req.url.includes('/api/auth/oauth2') ||
    req.url.includes('/api/auth/logout');

  if (token && !isPublicAuthEndpoint) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }

  return next(req).pipe(
    catchError((error) => {
      // If the backend rejects with 401 (invalid/expired token),
      // force logout and redirect to login — don't show a broken page.
      if (error.status === 401 && !isPublicAuthEndpoint) {
        auth.logout();
      }
      return throwError(() => error);
    })
  );
};
