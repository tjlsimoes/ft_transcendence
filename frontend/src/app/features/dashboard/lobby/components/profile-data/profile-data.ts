import { Component } from '@angular/core';
import { MOCK_PROFILE_DATA } from '../../../../../shared/models/lobby.mock';

@Component({
  selector: 'app-profile-data',
  imports: [],
  templateUrl: './profile-data.html',
  styleUrl: './profile-data.css',
})
export class ProfileData {
  rankTier = MOCK_PROFILE_DATA.rankTier;
  leagueName = MOCK_PROFILE_DATA.leagueName;
  seasonLabel = MOCK_PROFILE_DATA.seasonLabel;
  currentLp = MOCK_PROFILE_DATA.currentLp;
  targetLp = MOCK_PROFILE_DATA.targetLp;
  nextLeague = MOCK_PROFILE_DATA.nextLeague;

  get lpProgress(): number {
    const progress = (this.currentLp / this.targetLp) * 100;
    return Math.max(0, Math.min(100, progress));
  }
}
