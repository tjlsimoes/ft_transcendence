import { Injectable, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';

// Serviço compartilhado para estado de rota.
// Centraliza a lógica de detecção de rota lobby, evitando duplicação entre Navbar e Sidebar.
@Injectable({ providedIn: 'root' })
export class RouteStateService {
  private router = inject(Router);

  /** Signal reativo que indica se a rota atual pertence ao lobby. */
  isLobby = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(e => e.urlAfterRedirects.startsWith('/lobby'))
    ),
    { initialValue: this.router.url.startsWith('/lobby') }
  );
}
