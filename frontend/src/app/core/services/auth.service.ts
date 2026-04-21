import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { UserService } from './user.service';

export interface AuthResponse {
  token: string;
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
    localStorage.setItem(this.tokenKey, response.token);
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
    if (err.error?.error) return err.error.error;
    if (err.error?.message) return err.error.message;
    if (err.status === 0) return 'Unable to reach the server. Check your connection.';
    return 'An unexpected error occurred. Please try again.';
  }
}
