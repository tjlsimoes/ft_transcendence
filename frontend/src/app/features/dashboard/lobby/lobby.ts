import { Component, OnInit, signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { PlayerStats } from './components/player-stats/player-stats';
import { ProfileData } from './components/profile-data/profile-data';
import { TerminalHistory } from './components/terminal-history/terminal-history';
import { IdentityCard } from './components/identity-card/identity-card';
import { QueuePanel } from './components/queue-panel/queue-panel';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { LOBBY_TABS } from '../../../shared/models/lobby.mock';
import type { LobbyTab, PlayerIdentity, ProfileData as ProfileDataModel, SummaryStat, RecordStat } from '../../../shared/models/lobby.model';
import type { UserProfile, MatchHistory } from '../../../shared/models/user-profile.model';

@Component({
  selector: 'app-lobby',
  imports: [PlayerStats, ProfileData, TerminalHistory, IdentityCard, QueuePanel],
  templateUrl: './lobby.html',
  styleUrl: './lobby.css',
})
export class Lobby implements OnInit {
  tabs: LobbyTab[] = LOBBY_TABS;
  matchHistory = signal<MatchHistory[]>([]);
  matchHistoryError = signal(false);

  // Dados reais do utilizador autenticado.
  player = signal<PlayerIdentity>({ username: '...', league: '...', avatarUrl: '', isOnline: true });
  profileData = signal<ProfileDataModel>({ rankTier: '--', leagueName: '--', seasonLabel: '04', currentLp: 0, targetLp: 1000, nextLeague: '--' });
  summaryStats = signal<SummaryStat[]>([]);
  recordStats = signal<RecordStat[]>([]);

  constructor(
    private titleService: Title,
    private userService: UserService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.titleService.setTitle('Lobby — Code Arena');

    this.userService.loadMe().subscribe({
      next: (user) => this.applyUserData(user),
      error: () => {
        // Token inválido ou expirado — limpa sessão e redireciona para login.
        this.authService.logout();
      },
    });

    this.userService.loadMatches().subscribe({
      next: (matches) => {
        this.matchHistory.set(matches);
        this.matchHistoryError.set(false);
      },
      error: () => this.matchHistoryError.set(true),
    });
  }

  private applyUserData(user: UserProfile): void {
    const totalGames = user.wins + user.losses;
    const winRate = totalGames > 0 ? ((user.wins / totalGames) * 100).toFixed(1) : '0.0';

    // Identity card
    this.player.set({
      username: user.username,
      league: `${user.league} // ${user.elo} LP`,
      avatarUrl: user.avatarUrl ?? '',
      isOnline: true,
    });

    // Profile data (rank display)
    const leagueInfo = this.getLeagueInfo(user.league, user.elo, user);
    this.profileData.set({
      rankTier: leagueInfo.tier,
      leagueName: `${user.league} LEAGUE`,
      seasonLabel: '04',
      currentLp: leagueInfo.currentLp,
      targetLp: leagueInfo.targetLp,
      nextLeague: leagueInfo.nextLeague,
      legendThresholdLp: leagueInfo.legendThresholdLp,
      globalRank: leagueInfo.globalRank,
      highestLp: leagueInfo.highestLp,
    });

    // Summary stats
    this.summaryStats.set([
      { id: 'total-duels', label: 'Total Duels', value: totalGames.toLocaleString() },
      { id: 'win-rate', label: 'Win Rate', value: `${winRate}%`, highlight: true },
    ]);

    // Record stats
    this.recordStats.set([
      { id: 'wins', label: 'Wins', value: user.wins.toLocaleString() },
      { id: 'losses', label: 'Losses', value: user.losses.toLocaleString() },
      { id: 'streak', label: 'Win Streak', value: user.winStreak.toString(), accent: true },
    ]);
  }

  private getLeagueInfo(league: string, elo: number, user: UserProfile) {
    const leagues = ['BRONZE', 'SILVER', 'GOLD', 'MASTER', 'LEGEND'];
    const leagueIndex = leagues.indexOf(league);

    if (league === 'LEGEND') {
      return {
        tier: 'L',
        currentLp: elo,
        targetLp: elo, // No progression target for Legend
        nextLeague: 'LEGEND',
        globalRank: user.globalRank ?? undefined,
        highestLp: user.highestLp ?? undefined,
        legendThresholdLp: undefined,
      };
    }

    if (league === 'MASTER') {
      return {
        tier: 'M',
        currentLp: elo,
        targetLp: user.legendThresholdLp ?? elo,
        nextLeague: 'LEGEND',
        legendThresholdLp: user.legendThresholdLp ?? undefined,
        globalRank: undefined,
        highestLp: undefined,
      };
    }

    // Bronze, Silver, Gold — standard LP progress to next league
    const currentLp = elo % 1000;
    const nextLeague = leagueIndex < leagues.length - 1 ? leagues[leagueIndex + 1] : 'MASTER';

    return {
      tier: league.charAt(0),
      currentLp,
      targetLp: 1000,
      nextLeague,
      legendThresholdLp: undefined,
      globalRank: undefined,
      highestLp: undefined,
    };
  }
}
