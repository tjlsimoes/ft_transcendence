export interface LobbyUser {
  name: string;
  rank: string;
  avatarLetter: string;
  avatar?: string;
}

export interface Friend {
  id: string;
  name: string;
  avatar?: string;
  avatarLetter: string;
  status: 'online' | 'offline' | 'in-game';
  rank?: string;
}

export interface Notification {
  id: string;
  type: 'friend_request' | 'game_invite' | 'achievement';
  message: string;
  timestamp: Date;
  read: boolean;
}

export const LOBBY_NAVBAR_USER_MOCK: LobbyUser = {
  name: 'NULL_POINTER',
  rank: 'GOLD II // 2350 LP',
  avatarLetter: 'N',
  avatar: '',
};

export const FRIENDS_MOCK: Friend[] = [
  {
    id: '1',
    name: 'ByteKnight',
    avatar: '',
    avatarLetter: 'B',
    status: 'online',
    rank: 'PLATINUM I',
  },
  {
    id: '2',
    name: 'CodeSlayer',
    avatar: '',
    avatarLetter: 'C',
    status: 'in-game',
    rank: 'GOLD III',
  },
  {
    id: '3',
    name: 'StackOverflow',
    avatar: '',
    avatarLetter: 'S',
    status: 'online',
    rank: 'DIAMOND II',
  },
  {
    id: '4',
    name: 'NullRef',
    avatar: '',
    avatarLetter: 'N',
    status: 'offline',
    rank: 'SILVER I',
  },
];

export const NOTIFICATIONS_MOCK: Notification[] = [];
