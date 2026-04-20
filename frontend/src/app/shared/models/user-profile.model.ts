// Modelo do perfil do jogador devolvido por GET /api/users/me.
export interface UserProfile {
  id: number;
  username: string;
  email: string;
  avatarUrl: string | null;
  elo: number;
  wins: number;
  losses: number;
  winStreak: number;
  league: string;
  status: string;
  createdAt: string;

  // Ranking context — populated for Master/Legend players
  /** For MASTER: the minimum LP to enter Legend. Null for other leagues. */
  legendThresholdLp: number | null;
  /** For LEGEND: the player's current global rank position (1-based). Null for other leagues. */
  globalRank: number | null;
  /** For LEGEND: the highest LP among all players. Null for other leagues. */
  highestLp: number | null;
}

// Modelo de entrada do histórico de partidas devolvido por GET /api/users/me/matches.
export interface MatchHistory {
  id: number;
  result: 'VICTORY' | 'DEFEAT' | 'DRAW' | 'CANCELLED' | 'PENDING';
  opponent: string;
  status: string;
  lpChange: number | null;
  startedAt: string | null;
  endedAt: string | null;
}

// Modelo de amigo devolvido por GET /api/users/me/friends.
export interface FriendEntry {
  id: number;
  username: string;
  avatarUrl: string | null;
  league: string;
  status: string;
}
