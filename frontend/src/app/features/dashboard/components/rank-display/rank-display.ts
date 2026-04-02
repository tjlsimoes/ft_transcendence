import { Component, input } from '@angular/core';

export interface RankDisplayData {
  tierCode: string;
  leagueName: string;
  seasonLabel: string;
  recentResults: string;
  progressLabel: string;
  currentLp: number;
  targetLp: number;
  progressPercent: number;
  primaryAction: string;
  secondaryAction: string;
}

@Component({
  selector: 'app-rank-display',
  templateUrl: './rank-display.html',
  styleUrl: './rank-display.css',
})
export class RankDisplay {
  rank = input.required<RankDisplayData>();
}
