import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { PlayerStats } from './components/player-stats/player-stats';
import { ProfileData } from './components/profile-data/profile-data';
import { TerminalHistory } from './components/terminal-history/terminal-history';
import { IdentityCard } from './components/identity-card/identity-card';
import { QueuePanel } from './components/queue-panel/queue-panel';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { ChallengeService } from '../../../core/services/challenge.service';
import { MatchmakingStateService } from '../../../core/services/matchmaking-state.service';
import { MatchmakingEvent } from '../../../core/services/matchmaking.service';
import { LOBBY_TABS } from '../../../shared/models/lobby.mock';
import type { LobbyTab, PlayerIdentity, ProfileData as ProfileDataModel, SummaryStat, RecordStat } from '../../../shared/models/lobby.model';
import type { UserProfile, MatchHistory } from '../../../shared/models/user-profile.model';

@Component({
  selector: 'app-lobby',
  imports: [PlayerStats, ProfileData, TerminalHistory, IdentityCard, QueuePanel],
  templateUrl: './lobby.html',
  styleUrl: './lobby.css',
})
export class Lobby implements OnInit, OnDestroy {
  tabs: LobbyTab[] = LOBBY_TABS;
  matchHistory = signal<MatchHistory[]>([]);

  // Dados reais do utilizador autenticado.
  player = signal<PlayerIdentity>({ username: '...', league: '...', avatarUrl: '', isOnline: true });
  profileData = signal<ProfileDataModel>({ rankTier: '--', leagueName: '--', seasonLabel: '04', currentLp: 0, targetLp: 1000, nextLeague: '--' });
  summaryStats = signal<SummaryStat[]>([]);
  recordStats = signal<RecordStat[]>([]);

  constructor(
    private titleService: Title,
    private userService: UserService,
    private authService: AuthService,
    private wsService: WebSocketService,
    private challengeService: ChallengeService,
    private matchmakingState: MatchmakingStateService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.titleService.setTitle('Lobby — Code Arena');

    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    // Conectar WebSocket antes de qualquer interação de matchmaking.
    // O QueuePanel vai subscrever aos eventos depois de entrar na fila.
    const token = this.authService.getToken();
    if (token) {
      this.wsService.connect(token);
    }

    this.userService.loadMe().subscribe({
      next: (user) => this.applyUserData(user),
      error: () => {
        this.authService.logout();
      },
    });

    this.userService.loadMatches().subscribe({
      next: (matches) => this.matchHistory.set(matches),
    });
  }

  ngOnDestroy(): void {
    // Unsubscribe from local component observables if any, but do NOT disconnect the global WebSocket.
  }

  /**
   * Chamado pelo QueuePanel quando o backend confirma um match.
   * 1. Transiciona o overlay para "matched" com o nome do oponente.
   * 2. Após a animação (1.5 s), inicia o pré-carregamento do challenge.
   * 3. Quando o challenge está pronto, navega para a arena — o overlay
   *    permanece visível até a ArenaPage consumir o cache e o descartar.
   */
  onMatchFound(event: MatchmakingEvent): void {
    const opponentName = event.opponentName ?? 'Opponent';
    this.matchmakingState.onMatched(opponentName);

    const navigateToArena = () => {
      this.router.navigate(['/arena'], {
        queryParams: {
          duelId: event.duelId,
          challengeId: event.challengeId,
          opponent: opponentName,
        },
      });
    };

    const cid = event.challengeId;
    if (!cid) {
      // Sem challenge para pré-carregar: avança após animação.
      setTimeout(() => {
        this.matchmakingState.reset();
        navigateToArena();
      }, 1500);
      return;
    }

    // Aguarda a animação "OPPONENT FOUND" e depois inicia o loading.
    setTimeout(() => {
      this.matchmakingState.onLoading();

      this.challengeService.getChallenge(cid).subscribe({
        next: (challenge) => {
          this.matchmakingState.cacheChallenge(challenge);
          navigateToArena();
          // O overlay é descartado pela ArenaPage após consumir o cache.
        },
        error: () => {
          // Falha no pré-carregamento: navegar mesmo assim, a arena faz retry.
          this.matchmakingState.reset();
          navigateToArena();
        },
      });
    }, 1500);
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
