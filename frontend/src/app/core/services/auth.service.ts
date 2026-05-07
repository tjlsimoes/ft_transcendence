import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { finalize, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserService } from './user.service';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private userService = inject(UserService);
  private baseUrl = `${environment.apiUrl}/auth`;

  private tokenKey = 'auth_token';
  private refreshTokenKey = 'refresh_token';

  register(payload: RegisterPayload) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, payload);
  }

  login(payload: LoginPayload) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, payload);
  }

  /**
   * Exchanges the stored refresh token for a new access+refresh token pair.
   * Saves the new tokens to localStorage automatically via tap().
   * Called exclusively by the auth interceptor on 401 responses.
   */
  refreshTokens() {
    const refreshToken = this.getRefreshToken();
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/refresh`, { refreshToken })
      .pipe(tap((response) => this.saveToken(response)));
  }

  saveToken(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.accessToken);
    localStorage.setItem(this.refreshTokenKey, response.refreshToken);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  /**
   * Returns true when the session is considered active.
   *
   * Session validity is determined by the REFRESH token, not the access token.
   * The access token may be expired — the interceptor will transparently renew
   * it on the next HTTP request. Only when the refresh token itself is absent or
   * expired is the user truly logged out.
   *
   * Side effects:
   *  - Clears both tokens when the refresh token is expired (full cleanup).
   *  - Removes only the access token when it is expired but refresh is still
   *    valid, so the interceptor is not sent a stale Bearer header.
   */
  isLoggedIn(): boolean {
    const refreshToken = this.getRefreshToken();

    // No refresh token → definitely not logged in.
    if (!refreshToken || refreshToken === 'undefined' || refreshToken === 'null') {
      return false;
    }

    // Refresh token itself is expired → full cleanup, session over.
    if (this.isTokenExpired(refreshToken)) {
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.refreshTokenKey);
      return false;
    }

    // Refresh token is valid → session is active.
    // Remove the expired access token so it is not sent in the Authorization header
    // (the interceptor will obtain a fresh one on the first 401).
    const accessToken = this.getToken();
    if (accessToken && this.isTokenExpired(accessToken)) {
      localStorage.removeItem(this.tokenKey);
    }

    return true;
  }

  /**
   * Decodes the JWT payload (base64url) and checks the `exp` claim.
   * Does NOT verify the signature — that is the backend's responsibility.
   * This is only used client-side to avoid navigating with a known-expired token.
   */
  private isTokenExpired(token: string): boolean {
    try {
      const payloadBase64 = token.split('.')[1];
      // Convert base64url → standard base64, then add required padding.
      const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64 + '=='.slice(0, (4 - base64.length % 4) % 4);
      const decoded = JSON.parse(atob(padded));
      // `exp` is in seconds; Date.now() is in milliseconds.
      return typeof decoded.exp === 'number' && decoded.exp * 1000 < Date.now();
    } catch {
      // Malformed token — treat as expired.
      return true;
    }
  }

  logout(): void {
    const token = this.getToken();
    const refreshToken = this.getRefreshToken();

    const cleanup = () => {
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.refreshTokenKey);
      this.userService.clear();
      this.router.navigate(['/login']);
    };

    if (token) {
      const body = refreshToken ? { refreshToken } : {};
      // Wait for the backend to blacklist the tokens before clearing local state.
      // finalize() runs on both success and error, so UX is never blocked.
      this.http.post(`${this.baseUrl}/logout`, body)
        .pipe(finalize(cleanup))
        .subscribe();
    } else {
      cleanup();
    }
  }

  extractApiError(err: HttpErrorResponse): string {
    if (err.error?.error) return err.error.error;
    if (err.error?.message) return err.error.message;
    if (err.status === 0) return 'Unable to reach the server. Check your connection.';
    return 'An unexpected error occurred. Please try again.';
  }
}
