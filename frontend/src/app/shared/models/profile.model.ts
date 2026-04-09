// Modelo completo do perfil do jogador (resposta da API).
export interface PlayerProfile {
  id: string;
  username: string;
  email: string;
  avatarUrl?: string;
  avatarLetter: string;
  createdAt: Date;
  updatedAt: Date;
}

// Payload para atualização de dados pessoais (username ou email).
export interface UpdateProfilePayload {
  username?: string;
  email?: string;
}

// Payload para atualização de senha.
export interface UpdatePasswordPayload {
  currentPassword: string;
  newPassword: string;
}
