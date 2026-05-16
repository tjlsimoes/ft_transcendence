import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// Espelha o MatchmakingEvent do backend (MatchmakingEvent.java).
export interface MatchmakingEvent {
  type: 'QUEUED' | 'MATCHED' | 'TIMEOUT' | 'CANCELLED' | 'ERROR';
  duelId: number | null;
  opponentId: number | null;
  opponentName: string | null;
  challengeId: number | null;
  message: string;
}

export interface QueueStatus {
  queued: boolean;
  elo: number;
  status: string;
}

/**
 * Serviço HTTP para comunicar com os endpoints REST de matchmaking.
 *
 * POST   /api/matchmaking/queue        — entrar na fila
 * DELETE /api/matchmaking/queue        — sair da fila
 * GET    /api/matchmaking/queue/status — verificar estado na fila
 */
@Injectable({ providedIn: 'root' })
export class MatchmakingService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/matchmaking`;

  /** Adiciona o jogador autenticado à fila de matchmaking. */
  joinQueue(): Observable<MatchmakingEvent> {
    return this.http.post<MatchmakingEvent>(`${this.baseUrl}/queue`, {});
  }

  /** Remove o jogador autenticado da fila de matchmaking. */
  leaveQueue(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/queue`);
  }

  /** Verifica se o jogador está na fila e qual o seu estado. */
  getQueueStatus(): Observable<QueueStatus> {
    return this.http.get<QueueStatus>(`${this.baseUrl}/queue/status`);
  }
}
