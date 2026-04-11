// Modelos de dados do lobby e seus componentes.

export interface LobbyTab {
  id: string;
  label: string;
  active?: boolean;
}

export interface TerminalMatchHistory {
  id: string;
  result: 'VICTORY' | 'DEFEAT';
  lpChange: number;
  opponent: string;
  date: string;
}

export interface PlayerIdentity {
  username: string;
  league: string;
  avatarUrl: string;
  isOnline: boolean;
}

export interface ProfileData {
  rankTier: string;
  leagueName: string;
  seasonLabel: string;
  currentLp: number;
  targetLp: number;
  nextLeague: string;
}

export interface SummaryStat {
  id: string;
  label: string;
  value: string;
  highlight?: boolean;
}

export interface RecordStat {
  id: string;
  label: string;
  value: string;
  accent?: boolean;
}
