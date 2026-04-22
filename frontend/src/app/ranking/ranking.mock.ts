export type MatchResult = 'W' | 'L';

export interface WeeklyStat {
  label: string;
  value: string;
}

export interface LeaderboardPlayer {
  rank: number;
  name: string;
  league: string;
  tone: string;
  mark: string;
  elo: number;
  recentMatches: MatchResult[];
  weeklyStats: WeeklyStat[];
}

export const LEADERBOARD_PLAYERS_MOCK: LeaderboardPlayer[] = [
  {
    rank: 1,
    name: 'NyxCipher',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 4215,
    recentMatches: ['W', 'W', 'L'],
    weeklyStats: [
      { label: 'Win rate', value: '81%' },
      { label: 'LP', value: '+148' },
      { label: 'Duels', value: '26' },
    ],
  },
  {
    rank: 2,
    name: 'HexNova',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 4178,
    recentMatches: ['W', 'L', 'W'],
    weeklyStats: [
      { label: 'Win rate', value: '76%' },
      { label: 'LP', value: '+116' },
      { label: 'Duels', value: '22' },
    ],
  },
  {
    rank: 3,
    name: 'OrbitNull',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 4112,
    recentMatches: ['W', 'W', 'W'],
    weeklyStats: [
      { label: 'Win rate', value: '88%' },
      { label: 'LP', value: '+132' },
      { label: 'Duels', value: '17' },
    ],
  },
  {
    rank: 4,
    name: 'ShardPulse',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 4084,
    recentMatches: ['L', 'W', 'W'],
    weeklyStats: [
      { label: 'Win rate', value: '73%' },
      { label: 'LP', value: '+94' },
      { label: 'Duels', value: '21' },
    ],
  },
  {
    rank: 5,
    name: 'VantaLoop',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 4056,
    recentMatches: ['W', 'L', 'L'],
    weeklyStats: [
      { label: 'Win rate', value: '64%' },
      { label: 'LP', value: '+57' },
      { label: 'Duels', value: '19' },
    ],
  },
  {
    rank: 6,
    name: 'IonDrift',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 4033,
    recentMatches: ['W', 'W', 'L'],
    weeklyStats: [
      { label: 'Win rate', value: '71%' },
      { label: 'LP', value: '+79' },
      { label: 'Duels', value: '18' },
    ],
  },
  {
    rank: 7,
    name: 'QuartzByte',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 4018,
    recentMatches: ['L', 'W', 'W'],
    weeklyStats: [
      { label: 'Win rate', value: '69%' },
      { label: 'LP', value: '+61' },
      { label: 'Duels', value: '20' },
    ],
  },
  {
    rank: 8,
    name: 'PixelRook',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 3999,
    recentMatches: ['W', 'L', 'W'],
    weeklyStats: [
      { label: 'Win rate', value: '67%' },
      { label: 'LP', value: '+54' },
      { label: 'Duels', value: '16' },
    ],
  },
  {
    rank: 9,
    name: 'SilverStack',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 3974,
    recentMatches: ['W', 'W', 'L'],
    weeklyStats: [
      { label: 'Win rate', value: '70%' },
      { label: 'LP', value: '+47' },
      { label: 'Duels', value: '15' },
    ],
  },
  {
    rank: 10,
    name: 'EchoForge',
    league: 'Legend',
    tone: 'league-legend',
    mark: 'L',
    elo: 3951,
    recentMatches: ['L', 'W', 'L'],
    weeklyStats: [
      { label: 'Win rate', value: '61%' },
      { label: 'LP', value: '+31' },
      { label: 'Duels', value: '14' },
    ],
  },
];