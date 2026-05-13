import type { UserProfile } from '../models/user-profile.model';

export interface LeagueDisplayInfo {
  /** Single-letter tier abbreviation shown in the rank badge (B / S / G / M / L). */
  tier: string;
  /** LP progress within the current league bracket. */
  currentLp: number;
  /** LP target to reach the next league. */
  targetLp: number;
  /** Name of the next league to promote into. */
  nextLeague: string;
  /** For MASTER players: LP threshold to qualify for LEGEND. */
  legendThresholdLp?: number;
  /** For LEGEND players: global rank position (1-based). */
  globalRank?: number;
  /** For LEGEND players: highest LP among all players. */
  highestLp?: number;
}

const LEAGUE_ORDER = ['BRONZE', 'SILVER', 'GOLD', 'MASTER', 'LEGEND'] as const;

/**
 * Converts a user's league and elo into the display properties needed
 * by the lobby rank card.
 *
 * The `league` field is computed by the backend — this function only
 * maps it to UI presentation values; it does NOT re-derive the tier from elo.
 */
export function getLeagueDisplayInfo(user: UserProfile): LeagueDisplayInfo {
  const { league, elo } = user;
  const leagueIndex = LEAGUE_ORDER.indexOf(league as (typeof LEAGUE_ORDER)[number]);

  if (league === 'LEGEND') {
    return {
      tier: 'L',
      currentLp: elo,
      targetLp: elo,
      nextLeague: 'LEGEND',
      globalRank: user.globalRank ?? undefined,
      highestLp: user.highestLp ?? undefined,
    };
  }

  if (league === 'MASTER') {
    return {
      tier: 'M',
      currentLp: elo,
      targetLp: user.legendThresholdLp ?? elo,
      nextLeague: 'LEGEND',
      legendThresholdLp: user.legendThresholdLp ?? undefined,
    };
  }

  // Bronze, Silver, Gold — standard 1000-LP bracket progress
  const currentLp = elo % 1000;
  const nextLeague =
    leagueIndex >= 0 && leagueIndex < LEAGUE_ORDER.length - 1
      ? LEAGUE_ORDER[leagueIndex + 1]
      : 'MASTER';

  return {
    tier: league.charAt(0),
    currentLp,
    targetLp: 1000,
    nextLeague,
  };
}
