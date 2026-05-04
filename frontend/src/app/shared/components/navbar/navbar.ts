import { Component, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { RouteStateService } from '../../../core/services/route-state.service';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private routeState = inject(RouteStateService);
  private userService = inject(UserService);

  username = this.userService.username;
  avatarLetter = this.userService.avatarLetter;

  // Delegado ao serviço compartilhado para evitar duplicação de lógica de rota.
  isLobby = this.routeState.isLobby;
}
