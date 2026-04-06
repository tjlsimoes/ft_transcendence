import { Component, OnInit, signal } from '@angular/core';
import { LEADERBOARD_PLAYERS_MOCK, LeaderboardPlayer } from './ranking.mock';

@Component({
  selector: 'app-ranking',
  imports: [],
  templateUrl: './ranking.html',
  styleUrl: './ranking.css',
})
export class Ranking implements OnInit {
  protected readonly topPlayers = signal<LeaderboardPlayer[]>([]);

  ngOnInit(): void {
    this.loadRankingData();
  }

  private loadRankingData(): void {
    // Single mock source now; swap this call for API integration later.
    this.topPlayers.set(LEADERBOARD_PLAYERS_MOCK);
  }
}
