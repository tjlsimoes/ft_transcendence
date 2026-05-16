import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
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

  register(payload: RegisterPayload) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, payload);
  }

  login(payload: LoginPayload) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, payload);
  }

  saveToken(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.accessToken);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    const token = this.getToken();
    if (token) {
      // Notify the backend so the user is marked OFFLINE.
      this.http.post(`${this.baseUrl}/logout`, {}).subscribe();
    }
    localStorage.removeItem(this.tokenKey);
    this.userService.clear();
    this.router.navigate(['/login']);
  }

  extractApiError(err: HttpErrorResponse): string {
    if (err.status === 429) return 'Too many login attempts. Please wait a moment before trying again.';
    if (err.status === 401) return 'Invalid username or password.';
    if (err.status === 403) return 'Access denied. You do not have permission.';
    if (err.status >= 500) return 'Internal server error. The service might be temporarily down or restarting.';
    if (err.error?.error) return err.error.error;
    if (err.error?.message) return err.error.message;
    if (err.status === 0) return 'Unable to reach the server. Please check your internet connection or try again later.';
    return `An unexpected error occurred (Status: ${err.status} ${err.statusText || ''}). Please try again.`;
  }
}
