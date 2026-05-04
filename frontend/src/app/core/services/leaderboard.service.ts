import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LeaderboardEntry {
  rank: number;
  username: string;
  avatarUrl: string | null;
  elo: number;
  league: string;
  mark: string;
  tone: string;
  wins: number;
  losses: number;
  winStreak: number;
  totalDuels: number;
  winRate: string;
}

@Injectable({ providedIn: 'root' })
export class LeaderboardService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/leaderboard`;

  getLeaderboard(limit = 50): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(this.baseUrl, {
      params: { limit: limit.toString() },
    });
  }
}
