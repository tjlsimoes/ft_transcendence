import { Component, computed, input } from '@angular/core';
import type { ProfileData as ProfileDataModel } from '../../../../../shared/models/lobby.model';

@Component({
  selector: 'app-profile-data',
  imports: [],
  templateUrl: './profile-data.html',
  styleUrl: './profile-data.css',
})
export class ProfileData {
  data = input.required<ProfileDataModel>();

  rankTier = computed(() => this.data().rankTier);
  leagueName = computed(() => this.data().leagueName);
  seasonLabel = computed(() => this.data().seasonLabel);
  currentLp = computed(() => this.data().currentLp);
  targetLp = computed(() => this.data().targetLp);
  nextLeague = computed(() => this.data().nextLeague);

  // Ranking context for Master/Legend
  legendThresholdLp = computed(() => this.data().legendThresholdLp);
  globalRank = computed(() => this.data().globalRank);
  highestLp = computed(() => this.data().highestLp);

  isMaster = computed(() => this.data().leagueName === 'MASTER LEAGUE');
  isLegend = computed(() => this.data().leagueName === 'LEGEND LEAGUE');
  isStandardLeague = computed(() => !this.isMaster() && !this.isLegend());

  lpProgress = computed(() => {
    if (this.isLegend()) return 100;
    if (this.isMaster()) {
      const threshold = this.legendThresholdLp();
      if (!threshold || threshold <= 3000) return 0;
      const progress = ((this.currentLp() - 3000) / (threshold - 3000)) * 100;
      return Math.max(0, Math.min(100, progress));
    }
    const progress = (this.currentLp() / this.targetLp()) * 100;
    return Math.max(0, Math.min(100, progress));
  });
}
