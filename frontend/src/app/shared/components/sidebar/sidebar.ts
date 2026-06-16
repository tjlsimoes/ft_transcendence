import { Component, inject, signal, OnInit, computed, DestroyRef } from '@angular/core';
import { interval } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouteStateService } from '../../../core/services/route-state.service';
import { UserService } from '../../../core/services/user.service';
import type { FriendEntry } from '../../models/user-profile.model';
import { ChatStateService } from '../../../core/services/chat-state.service';
import { NotificationService } from '../../../core/services/notification.service';
import type { NotificationPayload } from '../../models/notification.model';
import { DatePipe } from '@angular/common';
import { statusClass } from '../../utils/status.utils';
import { AddFriendModal } from '../add-friend-modal/add-friend-modal';

@Component({
  selector: 'app-sidebar',
  imports: [DatePipe, AddFriendModal],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar implements OnInit {
  private routeState = inject(RouteStateService);
  private userService = inject(UserService);
  private chatStateService = inject(ChatStateService);
  notificationService = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  readonly statusClass = statusClass;
  username = this.userService.username;
  avatarLetter = this.userService.avatarLetter;
  friends = this.userService.friends;
  pendingRequests = this.userService.pendingRequests;
  totalUnread = computed(() => {
    const windowTotal = this.chatStateService.windows().reduce((sum, w) => sum + w.unread, 0);
    const pendingTotal = Object.values(this.chatStateService.pendingUnread())
                          .reduce((sum, n) => sum + n, 0);
    const friendRequestTotal = this.pendingRequests().length;
    // FRIEND_REQUEST is excluded here since it's already counted via friendRequestTotal above.
    const otherNotificationsTotal = this.notificationService.notifications()
      .filter(n => !n.read && n.type !== 'FRIEND_REQUEST')
      .length;
    return windowTotal + pendingTotal + friendRequestTotal + otherNotificationsTotal;
  }
  );

  activeTab = signal<'friends' | 'notifications'>('friends');
  showAddFriendModal = signal(false);

  // Delegado ao serviço compartilhado para evitar duplicação de lógica de rota.
  isLobby = this.routeState.isLobby;

  private loadFriends(): void {
    this.userService.loadFriends().subscribe({
      next: friends => this.chatStateService.syncFriendStatuses(friends)
    });
  }

  private loadPendingRequests(): void {
    this.userService.loadPendingRequests().subscribe();
  }

  ngOnInit(): void {
    if (this.isLobby()) {
      this.notificationService.init();
      this.loadFriends();
      this.loadPendingRequests();

      // Refresh every 30s so online/offline status and pending requests stay current
      interval(30_000)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
          this.loadFriends();
          this.loadPendingRequests();
        });
    }
  }

  openAddFriendModal(): void {
    this.showAddFriendModal.set(true);
  }

  closeAddFriendModal(): void {
    this.showAddFriendModal.set(false);
  }

  acceptRequest(requesterId: number): void {
    this.userService.acceptFriendRequest(requesterId).subscribe(() => {
      this.loadFriends();
      this.loadPendingRequests();
    });
  }

  rejectRequest(requesterId: number): void {
    this.userService.rejectFriendRequest(requesterId).subscribe(() => {
      this.loadPendingRequests();
    });
  }

  removeFriend(friendId: number, event: Event): void {
    event.stopPropagation();
    this.userService.removeFriend(friendId).subscribe(() => {
      this.loadFriends();
      this.chatStateService.closeConversation(friendId);
    });
  }

  setActiveTab(tab: 'friends' | 'notifications'): void {
    this.activeTab.set(tab);
    if (tab === 'notifications') {
      this.notificationService.markTabRead();
    }
  }

  getAvatarLetter(username: string): string {
    return username ? username.charAt(0).toUpperCase() : '?';
  }

  openChat(friend: FriendEntry): void {
    this.chatStateService.openConversation(friend);
    this.notificationService.markChatRead(friend.id);
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

  notificationLabel(n: NotificationPayload): string {
    if (n.type === 'FRIEND_REQUEST' || n.type === 'FRIEND_ACCEPTED') {
      const payload = n.payload as { username: string };
      return n.type === 'FRIEND_REQUEST'
        ? `${payload.username} sent you a friend request`
        : `${payload.username} accepted your friend request`;
    }
    return this.formatType(n.type);
  }
}
