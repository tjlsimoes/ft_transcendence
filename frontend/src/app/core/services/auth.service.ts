import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
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

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token || token === 'undefined' || token === 'null') {
      return false;
    }
    if (this.isTokenExpired(token)) {
      // Proactively remove stale tokens so subsequent checks are clean.
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.refreshTokenKey);
      return false;
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
