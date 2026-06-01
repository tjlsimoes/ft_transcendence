import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../shared/models/page.model';

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

  getLeaderboard(page = 0, size = 50, league?: string): Observable<Page<LeaderboardEntry>> {
    const params: any = {page: page.toString(), size: size.toString()};
    if (league)
      params.league = league;
    return this.http.get<Page<LeaderboardEntry>>(this.baseUrl, { params })
  }
}
