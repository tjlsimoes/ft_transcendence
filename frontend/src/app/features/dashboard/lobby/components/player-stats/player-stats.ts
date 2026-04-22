import { Component, input } from '@angular/core';
import type { SummaryStat, RecordStat } from '../../../../../shared/models/lobby.model';

@Component({
  selector: 'app-player-stats',
  imports: [],
  templateUrl: './player-stats.html',
  styleUrl: './player-stats.css',
})
export class PlayerStats {
  summaryStats = input<SummaryStat[]>([]);
  recordStats = input<RecordStat[]>([]);
}
