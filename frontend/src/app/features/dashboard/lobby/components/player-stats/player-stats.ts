import { Component } from '@angular/core';
import { MOCK_SUMMARY_STATS, MOCK_RECORD_STATS } from '../../../../../shared/models/lobby.mock';
import type { SummaryStat, RecordStat } from '../../../../../shared/models/lobby.model';

@Component({
  selector: 'app-player-stats',
  imports: [],
  templateUrl: './player-stats.html',
  styleUrl: './player-stats.css',
})
export class PlayerStats {
  summaryStats: SummaryStat[] = MOCK_SUMMARY_STATS;
  recordStats: RecordStat[] = MOCK_RECORD_STATS;
}
