import type {
  LobbyTab,
  TerminalMatchHistory,
  PlayerIdentity,
  ProfileData,
  SummaryStat,
  RecordStat,
} from './lobby.model';

// ──────────────────────────────────────────────────────────────────────
// Dados mock centralizados do lobby.
// Quando o backend estiver disponível, substituir por chamadas ao serviço.
// ──────────────────────────────────────────────────────────────────────

/** Abas do workspace editor (tabs fictícias do IDE theme). */
export const LOBBY_TABS: LobbyTab[] = [
  { id: 'lobby', label: 'lobby.html', active: true },
  { id: 'styles', label: 'lobby.css' },
];

/** Histórico de partidas recente. */
export const MOCK_MATCH_HISTORY: TerminalMatchHistory[] = [
  { id: 'match-1', result: 'VICTORY', lpChange: 24, opponent: 'xSniper99', date: '2 mins ago' },
  { id: 'match-2', result: 'DEFEAT', lpChange: -15, opponent: 'leet_coder', date: '1 hr ago' },
  { id: 'match-3', result: 'VICTORY', lpChange: 20, opponent: 'algo_master', date: 'Yesterday' },
  { id: 'match-4', result: 'DEFEAT', lpChange: -10, opponent: 'pro_gamer', date: '2 days ago' },
  { id: 'match-5', result: 'VICTORY', lpChange: 18, opponent: 'championX', date: '3 days ago' },
];

/** Identity card — dados do jogador logado (mock). */
export const MOCK_PLAYER_IDENTITY: PlayerIdentity = {
  username: 'NULL_POINTER',
  league: 'GOLD II',
  avatarUrl: 'https://api.dicebear.com/7.x/pixel-art/svg?seed=null_pointer',
  isOnline: true,
};

/** Dados de rank para o painel de profile-data. */
export const MOCK_PROFILE_DATA: ProfileData = {
  rankTier: 'G2',
  leagueName: 'GOLD LEAGUE',
  seasonLabel: '04',
  currentLp: 2350,
  targetLp: 3000,
  nextLeague: 'PLATINUM',
};

/** Resumo de estatísticas para o player-stats panel. */
export const MOCK_SUMMARY_STATS: SummaryStat[] = [
  { id: 'total-duels', label: 'Total Duels', value: '1,284' },
  { id: 'win-rate', label: 'Win Rate', value: '64.2%', highlight: true },
];

export const MOCK_RECORD_STATS: RecordStat[] = [
  { id: 'wins', label: 'Wins', value: '824' },
  { id: 'losses', label: 'Losses', value: '460' },
  { id: 'streak', label: 'Win Streak', value: '7', accent: true },
];

// Re-exporta tipos do model para conveniência.
export type {
  LobbyTab,
  TerminalMatchHistory,
  PlayerIdentity,
  ProfileData,
  SummaryStat,
  RecordStat,
} from './lobby.model';
