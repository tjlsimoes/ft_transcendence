import { Component, input, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import type { MatchHistory } from '../../../../../shared/models/user-profile.model';

@Component({
  selector: 'app-terminal-history',
  imports: [DatePipe],
  templateUrl: './terminal-history.html',
  styleUrl: './terminal-history.css',
})
export class TerminalHistory {
  matchHistory = input.required<MatchHistory[]>();
  hasMatches = computed(() => this.matchHistory().length > 0);
}
