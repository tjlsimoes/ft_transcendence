import { Component, inject, signal, OnInit, computed, DestroyRef } from '@angular/core';
import { interval } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouteStateService } from '../../../core/services/route-state.service';
import { UserService } from '../../../core/services/user.service';
import type { FriendEntry } from '../../models/user-profile.model';
import { ChatStateService } from '../../../core/services/chat-state.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  imports: [DatePipe],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar implements OnInit {
  private routeState = inject(RouteStateService);
  private userService = inject(UserService);
  private chatStateService = inject(ChatStateService);
  notificationService = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  username = this.userService.username;
  avatarLetter = this.userService.avatarLetter;
  friends = this.userService.friends;
  totalUnread = computed(() => {
    const windowTotal = this.chatStateService.windows().reduce((sum, w) => sum + w.unread, 0);
    const pendingTotal = Object.values(this.chatStateService.pendingUnread())
                          .reduce((sum, n) => sum + n, 0);
    return windowTotal + pendingTotal;
  }
  );

  activeTab = signal<'friends' | 'notifications'>('friends');

  // Delegado ao serviço compartilhado para evitar duplicação de lógica de rota.
  isLobby = this.routeState.isLobby;

  private loadFriends(): void {
    this.userService.loadFriends().subscribe();
  }

  ngOnInit(): void {
    if (this.isLobby()) {
      this.notificationService.init();
      this.loadFriends();

      // Refresh every 30s so online/offline status stays current
      interval(30_000)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => this.loadFriends());
    }
  }

  setActiveTab(tab: 'friends' | 'notifications'): void {
    this.activeTab.set(tab);
  }

  getAvatarLetter(username: string): string {
    return username ? username.charAt(0).toUpperCase() : '?';
  }

  openChat(friend: FriendEntry): void {
    this.chatStateService.openConversation(friend);
  }

  getUnread(friendId: number): number {
    const windowUnread = this.chatStateService.windows()
      .find(w => w.friend.id === friendId)?.unread ?? 0;
    const pending = this.chatStateService.pendingUnread()[friendId] ?? 0;
    return windowUnread + pending;
  }

  formatType(type: string): string {
    return type.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
  }
}
