import { Component, input } from '@angular/core';

export interface TerminalMatchHistory {
  result: 'VICTORY' | 'DEFEAT';
  lpChange: number;
  opponent: string;
  date: string;
}

@Component({
  selector: 'app-terminal-history',
  imports: [],
  templateUrl: './terminal-history.html',
  styleUrl: './terminal-history.css',
})
export class TerminalHistory {
  // Signal-based input (Angular 17+). Substitui o decorador @Input legado.
  matchHistory = input.required<TerminalMatchHistory[]>();
}
