import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { RouteStateService } from '../../../core/services/route-state.service';
import { LOBBY_USER_MOCK } from '../../models/user.mock';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private routeState = inject(RouteStateService);

  lobbyUser = LOBBY_USER_MOCK;

  // Delegado ao serviço compartilhado para evitar duplicação de lógica de rota.
  isLobby = this.routeState.isLobby;
}
