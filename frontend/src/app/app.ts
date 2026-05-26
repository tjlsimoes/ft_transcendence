import { Component, HostListener, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/components/navbar/navbar';
import { FloatingSymbols } from './shared/components/floating-symbols/floating-symbols';
import { Sidebar } from './shared/components/sidebar/sidebar';
import { AuthService } from './core/services/auth.service';
import { RouteStateService } from './core/services/route-state.service';
import { environment } from '../environments/environment';

// Componente raiz da aplicação: monta layout base (fundo, navbar e área de rotas).
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, FloatingSymbols, Sidebar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private authService = inject(AuthService);
  routeState = inject(RouteStateService);

  // sessionStorage sobrevive ao F5 (mesmo tab), mas é limpo ao fechar a aba.
  private readonly REFRESH_KEY = '__ca_refresh';
  private readonly PENDING_LOGOUT_KEY = '__ca_pending_logout';

  ngOnInit(): void {
    this.processDeferredLogout();
  }

  /**
   * Determina se o unload anterior foi um refresh (F5) ou fechamento de aba.
   * - Refresh: cancela o logout adiado — a sessão continua com o token ainda válido.
   * - Fechamento de aba: executa o logout adiado para marcar o usuário como OFFLINE
   *   e blacklistar o token.
   *
   * A detecção funciona porque sessionStorage é preservado no F5 do mesmo tab
   * mas é apagado quando o tab é fechado.
   */
  private processDeferredLogout(): void {
    const isRefresh = !!sessionStorage.getItem(this.REFRESH_KEY);
    const pendingLogoutRaw = localStorage.getItem(this.PENDING_LOGOUT_KEY);

    sessionStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.PENDING_LOGOUT_KEY);

    if (!pendingLogoutRaw) return;
    if (isRefresh) return; // F5 no mesmo tab: sessão continua, tokens ainda válidos.

    // Tab foi fechado: executa o logout adiado.
    try {
      const { token, refreshToken, timestamp } = JSON.parse(pendingLogoutRaw) as {
        token: string;
        refreshToken: string | null;
        timestamp: number;
      };
      const accessTokenExpiresAt = this.getJwtExpirationMs(token);
      // Ignora logout adiado quando o access token já expirou antes desta tentativa.
      if (accessTokenExpiresAt !== null && Date.now() > accessTokenExpiresAt) return;

      fetch(`${environment.apiUrl}/auth/logout`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: refreshToken ? JSON.stringify({ refreshToken }) : '{}',
      });
    } catch {
      // Dados corrompidos — ignora.
    }
  }

  /**
   * Dispara quando o usuário pressiona F5 ou fecha o tab/browser.
   * Em vez de chamar logout imediatamente (o que quebraria o F5), adia o logout
   * gravando no storage. Na próxima carga, processDeferredLogout() decide se
   * executa ou cancela o logout com base no tipo de unload detectado.
   */
  @HostListener('window:beforeunload')
  onBeforeUnload(): void {
    const token = this.authService.getToken();
    if (!token) return;

    // Esta flag sobrevive ao F5 (sessionStorage preservado) mas some ao fechar o tab.
    sessionStorage.setItem(this.REFRESH_KEY, '1');

    const refreshToken = this.authService.getRefreshToken();
    localStorage.setItem(
      this.PENDING_LOGOUT_KEY,
      JSON.stringify({ token, refreshToken, timestamp: Date.now() })
    );
  }

  private getJwtExpirationMs(token: string): number | null {
    try {
      const payloadBase64 = token.split('.')[1];
      if (!payloadBase64) return null;
      const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64 + '=='.slice(0, (4 - base64.length % 4) % 4);
      const decoded = JSON.parse(atob(padded));
      if (typeof decoded.exp !== 'number') return null;
      return decoded.exp * 1000;
    } catch {
      return null;
    }
  }
}
