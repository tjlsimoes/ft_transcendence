import { ActivityLogItem } from '../components/activity-log/activity-log';
import { FriendItem } from '../components/friends-list/friends-list';
import { PlayerStatsData } from '../components/player-stats/player-stats';
import { RankDisplayData } from '../components/rank-display/rank-display';
import { StatusBarData } from '../components/status-bar/status-bar';

export interface TransmissionItem {
  difficulty: string;
  language: string;
  leftPlayer: string;
  rightPlayer: string;
  timer: string;
}

export interface LobbyMockData {
  activeDuelsCount: number;
  playerStats: PlayerStatsData;
  friends: FriendItem[];
  rank: RankDisplayData;
  transmissions: TransmissionItem[];
  logs: ActivityLogItem[];
  status: StatusBarData;
}

export const LOBBY_MOCK_DATA: LobbyMockData = {
  activeDuelsCount: 42,
  playerStats: {
    totalDuels: '1,221',
    winRate: '64.2%',
    wins: '824',
    losses: '460',
    winStreak: '7',
    skills: [
      { label: 'Algorithms', value: 88 },
      { label: 'Data Structures', value: 72 },
      { label: 'Dynamic Prog.', value: 65 },
      { label: 'Graph Theory', value: 51 },
    ],
  },
  friends: [
    { nickname: 'Maria_Dev', status: 'online', actionLabel: 'Invite' },
    { nickname: 'Alex_Cipher', status: 'in-duel', actionLabel: 'In Duel' },
    { nickname: 'ByteMaster', status: 'online', actionLabel: 'Invite' },
    { nickname: 'Ghost_User', status: 'offline', actionLabel: 'Offline' },
    { nickname: 'RootAccess', status: 'offline', actionLabel: 'Offline' },
  ],
  rank: {
    tierCode: 'G2',
    leagueName: 'Gold League',
    seasonLabel: 'Ranked Season 04',
    recentResults: 'W W L W W',
    progressLabel: 'Progress to Platinum',
    currentLp: 2350,
    targetLp: 3000,
    progressPercent: 78.33,
    primaryAction: 'Enter Ranked Queue',
    secondaryAction: 'Practice',
  },
  transmissions: [
    {
      difficulty: 'Medium',
      language: 'Python',
      leftPlayer: 'ByteMaster',
      rightPlayer: 'LogicBomb',
      timer: '04:22',
    },
    {
      difficulty: 'Hard',
      language: 'C++',
      leftPlayer: 'KernelPanic',
      rightPlayer: 'RootUser',
      timer: '12:05',
    },
    {
      difficulty: 'Easy',
      language: 'JavaScript',
      leftPlayer: 'NullRef',
      rightPlayer: 'StackOverflow',
      timer: '01:47',
    },
    {
      difficulty: 'Medium',
      language: 'Rust',
      leftPlayer: 'DataDragon',
      rightPlayer: 'BitFlip',
      timer: '08:33',
    },
  ],
  logs: [
    {
      time: '14:22:05',
      level: 'success',
      title: 'SUCCESS',
      message: 'You defeated Alex_Cipher in a Medium duel. +32 LP [rank up predicted].',
    },
    {
      time: '13:45:12',
      level: 'info',
      title: 'INFO',
      message: 'Friend Maria_Dev is now online.',
    },
    {
      time: '12:10:00',
      level: 'warning',
      title: 'NEW CHALLENGE',
      message: 'Binary Maze is now available in the arena.',
    },
    {
      time: '11:02:44',
      level: 'info',
      title: 'INFO',
      message: 'Weekly leaderboard rewards have been distributed.',
    },
    {
      time: '09:15:22',
      level: 'success',
      title: 'SUCCESS',
      message: 'Daily streak maintained. Day 14.',
    },
  ],
  status: {
    server: 'US-EAST-1',
    onlineStatus: 'Online',
    latency: '24ms',
    build: 'v2.4.0-stable',
    copyright: '2024 Code Arena Interactive',
  },
};
