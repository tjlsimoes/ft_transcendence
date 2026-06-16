import { Component, DestroyRef, inject, signal } from '@angular/core';
import { NavigationCancel, NavigationEnd, NavigationError, NavigationSkipped, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DuelService } from './core/services/duel.service';
import { Navbar } from './shared/components/navbar/navbar';
import { FloatingSymbols } from './shared/components/floating-symbols/floating-symbols';
import { Sidebar } from './shared/components/sidebar/sidebar';
import { Footer } from './shared/components/footer/footer';
import { RouteStateService } from './core/services/route-state.service';
import { ChatWindowsContainer } from "./shared/components/chat-windows-container/chat-windows-container";

// Componente raiz da aplicação: monta layout base (fundo, navbar e área de rotas).
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, FloatingSymbols, Sidebar, Footer, ChatWindowsContainer],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private http = inject(HttpClient);
  private duelService = inject(DuelService);

  routeState = inject(RouteStateService);
  readonly isNavigating = signal(true);
  readonly isBackendReady = signal(false);
  readonly isArenaNavigationLoading = signal(false);
  readonly arenaLoadingOpponentName = signal('Opponent');
  readonly state = {
    opponentName: this.arenaLoadingOpponentName,
  };

  constructor() {
    this.checkBackendHealth();

    this.router.events
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event) => {
        if (event instanceof NavigationStart) {
          this.isNavigating.set(true);
          const navigatingToArena = event.url.startsWith('/arena');
          this.isArenaNavigationLoading.set(navigatingToArena);

          if (navigatingToArena) {
            this.loadArenaOpponentName();
          }
        }

        if (
          event instanceof NavigationEnd ||
          event instanceof NavigationCancel ||
          event instanceof NavigationError ||
          event instanceof NavigationSkipped
        ) {
          this.isNavigating.set(false);
          this.isArenaNavigationLoading.set(false);
        }
      });
  }

  private loadArenaOpponentName(): void {
    this.arenaLoadingOpponentName.set('Opponent');

    this.duelService.getActiveDuel().subscribe({
      next: (activeDuel) => {
        const opponent = activeDuel?.opponentName?.trim();
        if (opponent) {
          this.arenaLoadingOpponentName.set(opponent);
        }
      },
      error: () => {
        // Keep the fallback text while duel context finishes syncing.
      },
    });
  }

  private checkBackendHealth() {
    this.http.get('/api/health').subscribe({
      next: (res: any) => {
        if (res && res.status === 'UP') {
          this.isBackendReady.set(true);
          this.router.initialNavigation();
        } else {
          setTimeout(() => this.checkBackendHealth(), 2000);
        }
      },
      error: () => {
        setTimeout(() => this.checkBackendHealth(), 2000);
      }
    });
  }
}
