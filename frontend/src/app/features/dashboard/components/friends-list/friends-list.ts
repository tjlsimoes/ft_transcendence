import { Component, input } from '@angular/core';

export type FriendStatus = 'online' | 'in-duel' | 'offline';

export interface FriendItem {
  nickname: string;
  status: FriendStatus;
  actionLabel: string;
}

@Component({
  selector: 'app-friends-list',
  templateUrl: './friends-list.html',
  styleUrl: './friends-list.css',
})
export class FriendsList {
  friends = input.required<FriendItem[]>();

  statusLabel(status: FriendStatus): string {
    if (status === 'in-duel') return 'In Duel';
    if (status === 'offline') return 'Offline';
    return 'Online';
  }
}
