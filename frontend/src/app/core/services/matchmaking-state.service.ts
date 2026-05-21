import { Injectable, signal } from '@angular/core';
import { Subject } from 'rxjs';
import { ChallengeResponse } from './challenge.service';

export type MatchmakingPhase = 'idle' | 'searching' | 'matched' | 'loading';

/**
 * Serviço global que controla o estado do overlay de matchmaking.
 *
 * Fluxo:
 *  searching → matched (match encontrado) → loading (challenge a buscar) → idle (navegar)
 *
 * O QueuePanel atualiza o estado ao entrar/sair da fila.
 * O Lobby pré-carrega o challenge e navega para a arena.
 * A ArenaPage consome o challenge em cache e descarta o overlay.
 */
@Injectable({ providedIn: 'root' })
export class MatchmakingStateService {
  readonly phase = signal<MatchmakingPhase>('idle');
  readonly queueTime = signal('00:00');
  readonly opponentName = signal('');

  /** Emitido quando o utilizador cancela via overlay. O QueuePanel escuta isto. */
  readonly cancelRequested$ = new Subject<void>();

  private _cachedChallenge: ChallengeResponse | null = null;

  startSearching(): void {
    this.phase.set('searching');
  }

  setQueueTime(time: string): void {
    this.queueTime.set(time);
  }

  onMatched(opponentName: string): void {
    this.opponentName.set(opponentName);
    this.phase.set('matched');
  }

  onLoading(): void {
    this.phase.set('loading');
  }

  cacheChallenge(challenge: ChallengeResponse): void {
    this._cachedChallenge = challenge;
  }

  /** Retorna o challenge em cache e limpa-o (use uma vez, na ArenaPage). */
  consumeCachedChallenge(): ChallengeResponse | null {
    const c = this._cachedChallenge;
    this._cachedChallenge = null;
    return c;
  }

  /** Solicita cancelamento — o QueuePanel executa o HTTP DELETE. */
  requestCancel(): void {
    this.cancelRequested$.next();
  }

  /** Repõe o overlay para o estado invisível. */
  reset(): void {
    this.phase.set('idle');
    this.queueTime.set('00:00');
    this.opponentName.set('');
    this._cachedChallenge = null;
  }
}
