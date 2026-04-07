import { Component } from '@angular/core';

@Component({
  selector: 'app-profile-data',
  imports: [],
  templateUrl: './profile-data.html',
  styleUrl: './profile-data.css',
})
export class ProfileData {
  rankTier = 'G2';
  leagueName = 'GOLD LEAGUE';
  seasonLabel = '04';
  currentLp = 2350;
  targetLp = 3000;
  nextLeague = 'PLATINUM';

  get lpProgress(): number {
    const progress = (this.currentLp / this.targetLp) * 100;
    return Math.max(0, Math.min(100, progress));
  }
}

