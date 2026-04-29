import { Component, HostListener, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/components/navbar/navbar';
import { FloatingSymbols } from './shared/components/floating-symbols/floating-symbols';
import { Sidebar } from './shared/components/sidebar/sidebar';
import { AuthService } from './core/services/auth.service';
import { environment } from '../environments/environment';

// Componente raiz da aplicação: monta layout base (fundo, navbar e área de rotas).
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, FloatingSymbols, Sidebar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private authService = inject(AuthService);

  /**
   * Fires when the user closes the tab or navigates away from the app.
   * Uses fetch with keepalive:true because Angular's HttpClient is cancelled
   * during page unload. This ensures the backend marks the user as OFFLINE
   * and blacklists their tokens even when the browser is closed.
   */
  @HostListener('window:beforeunload')
  onBeforeUnload(): void {
    const token = this.authService.getToken();
    if (!token) return;

    const refreshToken = this.authService.getRefreshToken();
    const body = refreshToken ? JSON.stringify({ refreshToken }) : '{}';

    fetch(`${environment.apiUrl}/auth/logout`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body,
      keepalive: true,
    });
  }
}
