import { Component, DestroyRef, inject, signal } from '@angular/core';
import { NavigationCancel, NavigationEnd, NavigationError, NavigationSkipped, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Navbar } from './shared/components/navbar/navbar';
import { FloatingSymbols } from './shared/components/floating-symbols/floating-symbols';
import { Sidebar } from './shared/components/sidebar/sidebar';
import { RouteStateService } from './core/services/route-state.service';
import { ChatWindowsContainer } from "./shared/components/chat-windows-container/chat-windows-container";

// Componente raiz da aplicação: monta layout base (fundo, navbar e área de rotas).
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, FloatingSymbols, Sidebar, ChatWindowsContainer],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  routeState = inject(RouteStateService);
  readonly isNavigating = signal(true);

  constructor() {
    this.router.events
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event) => {
        if (event instanceof NavigationStart) {
          this.isNavigating.set(true);
        }

        if (
          event instanceof NavigationEnd ||
          event instanceof NavigationCancel ||
          event instanceof NavigationError ||
          event instanceof NavigationSkipped
        ) {
          this.isNavigating.set(false);
        }
      });
  }
}
