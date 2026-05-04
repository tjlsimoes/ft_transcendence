import { Component, inject, signal, OnInit } from '@angular/core';
import { RouteStateService } from '../../../core/services/route-state.service';
import { UserService } from '../../../core/services/user.service';
import type { FriendEntry } from '../../models/user-profile.model';

@Component({
  selector: 'app-sidebar',
  imports: [],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar implements OnInit {
  private routeState = inject(RouteStateService);
  private userService = inject(UserService);

  username = this.userService.username;
  avatarLetter = this.userService.avatarLetter;
  friends = signal<FriendEntry[]>([]);

  activeTab = signal<'friends' | 'notifications'>('friends');

  // Delegado ao serviço compartilhado para evitar duplicação de lógica de rota.
  isLobby = this.routeState.isLobby;

  ngOnInit(): void {
    if (this.isLobby()) {
      this.userService.loadFriends().subscribe({
        next: (friends) => this.friends.set(friends),
      });
    }
  }

  setActiveTab(tab: 'friends' | 'notifications'): void {
    this.activeTab.set(tab);
  }

  getAvatarLetter(username: string): string {
    return username ? username.charAt(0).toUpperCase() : '?';
  }
}
