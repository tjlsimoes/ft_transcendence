import { Component, inject, signal } from '@angular/core';
import { RouteStateService } from '../../../core/services/route-state.service';
import { LOBBY_USER_MOCK, FRIENDS_MOCK, Friend } from '../../models/user.mock';

@Component({
  selector: 'app-sidebar',
  imports: [],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  private routeState = inject(RouteStateService);

  user = LOBBY_USER_MOCK;
  friends: Friend[] = FRIENDS_MOCK;

  activeTab = signal<'friends' | 'notifications'>('friends');

  // Delegado ao serviço compartilhado para evitar duplicação de lógica de rota.
  isLobby = this.routeState.isLobby;

  setActiveTab(tab: 'friends' | 'notifications'): void {
    this.activeTab.set(tab);
  }
}
