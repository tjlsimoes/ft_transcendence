import { Component, inject, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';
import { LOBBY_NAVBAR_USER_MOCK, FRIENDS_MOCK, Friend } from './sidebar.mock';

@Component({
  selector: 'app-sidebar',
  imports: [],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  private router = inject(Router);

  user = LOBBY_NAVBAR_USER_MOCK;
  friends: Friend[] = FRIENDS_MOCK;
  
  activeTab = signal<'friends' | 'notifications'>('friends');

  isLobby = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(e => e.urlAfterRedirects.startsWith('/lobby'))
    ),
    { initialValue: this.router.url.startsWith('/lobby') }
  );

  setActiveTab(tab: 'friends' | 'notifications'): void {
    this.activeTab.set(tab);
  }
}
