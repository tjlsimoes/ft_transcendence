import { Component, input } from '@angular/core';

export interface SkillItem {
  label: string;
  value: number;
}

export interface PlayerStatsData {
  totalDuels: string;
  winRate: string;
  wins: string;
  losses: string;
  winStreak: string;
  skills: SkillItem[];
}

@Component({
  selector: 'app-player-stats',
  templateUrl: './player-stats.html',
  styleUrl: './player-stats.css',
})
export class PlayerStats {
  stats = input.required<PlayerStatsData>();
}
