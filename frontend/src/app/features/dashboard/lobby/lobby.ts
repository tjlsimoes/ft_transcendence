import { Component } from '@angular/core';
import { RankDisplay } from '../components/rank-display/rank-display';
import { PlayerStats } from '../components/player-stats/player-stats';
import { FriendsList } from '../components/friends-list/friends-list';
import { ActivityLog } from '../components/activity-log/activity-log';
import { StatusBar } from '../components/status-bar/status-bar';
import { LOBBY_MOCK_DATA } from './lobby.mock';

@Component({
  selector: 'app-lobby',
  imports: [RankDisplay, PlayerStats, FriendsList, ActivityLog, StatusBar],
  templateUrl: './lobby.html',
  styleUrl: './lobby.css',
})
export class Lobby {
  activeDuelsCount = LOBBY_MOCK_DATA.activeDuelsCount;
  playerStats = LOBBY_MOCK_DATA.playerStats;
  friends = LOBBY_MOCK_DATA.friends;
  rank = LOBBY_MOCK_DATA.rank;
  transmissions = LOBBY_MOCK_DATA.transmissions;
  logs = LOBBY_MOCK_DATA.logs;
  status = LOBBY_MOCK_DATA.status;
}
