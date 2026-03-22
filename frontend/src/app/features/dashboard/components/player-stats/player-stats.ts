import { Component, Input } from '@angular/core';

export type PlayerStatRow = {
  label: string;
  value: string;
  accent?: boolean;
};

export type PlayerStatsViewModel = {
  totalDuels: string;
  winRate: string;
  rows: PlayerStatRow[];
};

export const MOCK_PLAYER_STATS: PlayerStatsViewModel = {
  totalDuels: '1,284',
  winRate: '64.2%',
  rows: [
    { label: 'Wins', value: '824' },
    { label: 'Losses', value: '460' },
    { label: 'Win Streak', value: '7', accent: true },
  ],
};

@Component({
  selector: 'app-player-stats',
  templateUrl: './player-stats.html',
  styleUrl: './player-stats.css',
})
export class PlayerStats {
  @Input() stats: PlayerStatsViewModel = MOCK_PLAYER_STATS;
}
