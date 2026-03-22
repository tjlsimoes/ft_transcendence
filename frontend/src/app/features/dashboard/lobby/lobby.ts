import { Component } from '@angular/core';
import { PlayerStats } from '../components/player-stats/player-stats';
import { FriendsList } from '../components/friends-list/friends-list';
import { RankDisplay } from '../components/rank-display/rank-display';
import { LiveDuels } from '../components/live-duels/live-duels';
import { ActivityLog } from '../components/activity-log/activity-log';
import { StatusBar } from '../components/status-bar/status-bar';

@Component({
  selector: 'app-lobby',
  imports: [PlayerStats, FriendsList, RankDisplay, LiveDuels, ActivityLog, StatusBar],
  templateUrl: './lobby.html',
  styleUrl: './lobby.css',
})
export class Lobby {}
