import { Component, inject } from '@angular/core';
import { MatchmakingStateService } from '../../../core/services/matchmaking-state.service';

@Component({
  selector: 'app-matchmaking-overlay',
  imports: [],
  templateUrl: './matchmaking-overlay.html',
  styleUrl: './matchmaking-overlay.css',
})
export class MatchmakingOverlay {
  readonly state = inject(MatchmakingStateService);

  cancel(): void {
    this.state.requestCancel();
  }
}
