// Modelo de usuário do lobby (navbar + sidebar).
export interface LobbyUser {
  name: string;
  rank: string;
  avatarLetter: string;
  avatar?: string;
}

// Modelo de amigo na lista de friends.
export interface Friend {
  id: string;
  name: string;
  avatar?: string;
  avatarLetter: string;
  status: 'online' | 'offline' | 'in-game';
  rank?: string;
}

// Modelo de notificação no painel lateral.
export interface Notification {
  id: string;
  type: 'friend_request' | 'game_invite' | 'achievement';
  message: string;
  timestamp: Date;
  read: boolean;
}
