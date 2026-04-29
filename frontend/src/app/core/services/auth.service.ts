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
    return !!token && token !== 'undefined' && token !== 'null';
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
