import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  // Don't send tokens to login/register — they don't need them,
  // and a stale token would cause unnecessary JWT validation.
  // Logout DOES need the token so the backend can identify the user.
  const isPublicAuthEndpoint =
    req.url.includes('/api/auth/login') || req.url.includes('/api/auth/register');

  if (token && !isPublicAuthEndpoint) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }

  return next(req);
};
