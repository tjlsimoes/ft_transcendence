import { PlayerProfile } from './profile.model';

// Mock do perfil completo do jogador (simula resposta da API).
export const PLAYER_PROFILE_MOCK: PlayerProfile = {
  id: 'usr_001',
  username: 'NULL_POINTER',
  email: 'null_pointer@codearena.dev',
  avatarLetter: 'N',
  createdAt: new Date('2025-11-15T10:30:00Z'),
  updatedAt: new Date('2026-04-07T14:22:00Z'),
};

// Re-exporta tipos do model para conveniência.
export type { PlayerProfile, UpdateProfilePayload, UpdatePasswordPayload } from './profile.model';
