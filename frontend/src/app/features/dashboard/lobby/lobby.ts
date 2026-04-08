import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { PlayerStats } from './components/player-stats/player-stats';
import { ProfileData } from './components/profile-data/profile-data';
import { TerminalHistory } from './components/terminal-history/terminal-history';
import { IdentityCard } from './components/identity-card/identity-card';
import { QueuePanel } from './components/queue-panel/queue-panel';
import {
  LOBBY_TABS,
  MOCK_MATCH_HISTORY,
  MOCK_PLAYER_IDENTITY,
} from '../../../shared/models/lobby.mock';
import type { LobbyTab, TerminalMatchHistory } from '../../../shared/models/lobby.model';

@Component({
  selector: 'app-lobby',
  imports: [PlayerStats, ProfileData, TerminalHistory, IdentityCard, QueuePanel],
  templateUrl: './lobby.html',
  styleUrl: './lobby.css',
})
export class Lobby implements OnInit {
  tabs: LobbyTab[] = LOBBY_TABS;
  matchHistory: TerminalMatchHistory[] = MOCK_MATCH_HISTORY;
  player = MOCK_PLAYER_IDENTITY;

  constructor(private titleService: Title) {}

  ngOnInit(): void {
    this.titleService.setTitle('Lobby — Code Arena');
  }
}
