import { Component, OnInit, signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { LeaderboardService, LeaderboardEntry } from '../../core/services/leaderboard.service';

@Component({
  selector: 'app-ranking',
  imports: [],
  templateUrl: './ranking.html',
  styleUrl: './ranking.css',
})
export class Ranking implements OnInit {
  protected readonly players = signal<LeaderboardEntry[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);

  constructor(
    private titleService: Title,
    private leaderboardService: LeaderboardService,
  ) {}

  ngOnInit(): void {
    this.titleService.setTitle('Leaderboard — Code Arena');
    this.leaderboardService.getLeaderboard().subscribe({
      next: (data) => {
        this.players.set(data.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }
}
