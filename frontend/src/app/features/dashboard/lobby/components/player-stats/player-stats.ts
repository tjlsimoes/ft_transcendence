import { Component } from '@angular/core';

interface SummaryStat {
  id: string;
  label: string;
  value: string;
  highlight?: boolean;
}

interface RecordStat {
  id: string;
  label: string;
  value: string;
  accent?: boolean;
}

@Component({
  selector: 'app-player-stats',
  imports: [],
  templateUrl: './player-stats.html',
  styleUrl: './player-stats.css',
})
export class PlayerStats {
  summaryStats: SummaryStat[] = [
    {
      id: 'total-duels',
      label: 'Total Duels',
      value: '1,284',
    },
    {
      id: 'win-rate',
      label: 'Win Rate',
      value: '64.2%',
      highlight: true,
    },
  ];

  recordStats: RecordStat[] = [
    {
      id: 'wins',
      label: 'Wins',
      value: '824',
    },
    {
      id: 'losses',
      label: 'Losses',
      value: '460',
    },
    {
      id: 'streak',
      label: 'Win Streak',
      value: '7',
      accent: true,
    },
  ];
}
