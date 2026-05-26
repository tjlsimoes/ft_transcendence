import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

// Module-level state shared across all interceptor invocations.
// Guards against firing multiple parallel refresh requests when several
// requests get a 401 simultaneously (e.g. after token expiry).
let isRefreshing = false;
type RefreshTokenState =
  | { status: 'idle' }
  | { status: 'success'; accessToken: string }
  | { status: 'failed' };

const refreshTokenSubject = new BehaviorSubject<RefreshTokenState>({ status: 'idle' });

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  // These endpoints never need a Bearer token — and /refresh must be excluded
  // to avoid infinite loops when the refresh call itself gets a 401.
  const isPublicAuthEndpoint =
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/auth/register') ||
    req.url.includes('/api/auth/refresh');

  const token = auth.getToken();
  if (token && !isPublicAuthEndpoint) {
    req = attachToken(req, token);
  }

  return next(req).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !isPublicAuthEndpoint
      ) {
        return handle401(req, next, auth);
      }
      return throwError(() => error);
    }),
  );
};

function attachToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

function handle401(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  auth: AuthService,
) {
  // No refresh token on disk — nothing to try, force logout.
  if (!auth.getRefreshToken()) {
    auth.logout();
    return throwError(() => new Error('Session expired'));
  }

  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next({ status: 'idle' }); // signal "refresh in progress" to queued requests

    return auth.refreshTokens().pipe(
      switchMap((response) => {
        isRefreshing = false;
        refreshTokenSubject.next({ status: 'success', accessToken: response.accessToken });
        return next(attachToken(req, response.accessToken));
      }),
      catchError((err) => {
        isRefreshing = false;
        refreshTokenSubject.next({ status: 'failed' }); // unblock queued requests with terminal state
        auth.logout(); // refresh also failed → force logout
        return throwError(() => err);
      }),
    );
  }

  // Another request is already refreshing — queue until the new token arrives.
  return refreshTokenSubject.pipe(
    filter((state) => state.status !== 'idle'),
    take(1),
    switchMap((state) => {
      if (state.status === 'success') {
        return next(attachToken(req, state.accessToken));
      }
      return throwError(() => new Error('Session expired'));
    }),
  );
}
