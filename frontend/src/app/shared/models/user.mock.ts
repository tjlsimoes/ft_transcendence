import { LobbyUser, Friend, Notification } from './user.model';

// Mock do usuário logado, compartilhado entre Navbar e Sidebar.
export const LOBBY_USER_MOCK: LobbyUser = {
  name: 'NULL_POINTER',
  rank: 'GOLD II // 2350 LP',
  avatarLetter: 'N',
  avatar: 'https://i.pravatar.cc/150?img=68',
};

// Mock da lista de amigos exibida na Sidebar.
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

// Mock de notificações (vazio por padrão).
export const NOTIFICATIONS_MOCK: Notification[] = [];

// Re-exporta tipos do model para conveniência.
export type { LobbyUser, Friend, Notification } from './user.model';
