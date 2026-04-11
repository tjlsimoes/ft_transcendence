import { Component, input } from '@angular/core';
import type { TerminalMatchHistory } from '../../../../../shared/models/lobby.model';

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
