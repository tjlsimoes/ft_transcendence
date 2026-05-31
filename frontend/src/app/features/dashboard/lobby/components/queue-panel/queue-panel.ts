import { Component, OnDestroy, Output, EventEmitter, signal, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { MatchmakingService, MatchmakingEvent } from '../../../../../core/services/matchmaking.service';
import { WebSocketService } from '../../../../../core/services/websocket.service';

@Component({
  selector: 'app-queue-panel',
  imports: [],
  templateUrl: './queue-panel.html',
  styleUrl: './queue-panel.css',
})
export class QueuePanel implements OnDestroy {
  /** Emitido quando o backend confirma um match. O Lobby trata a navegação. */
  @Output() matchFound = new EventEmitter<MatchmakingEvent>();

  readonly isQueueing = signal(false);
  readonly queueTime = signal('00:00');
  readonly errorMessage = signal<string | null>(null);

  private queueInterval: ReturnType<typeof setInterval> | null = null;
  private wsSub: Subscription | null = null;

  private matchmakingService = inject(MatchmakingService);
  private wsService = inject(WebSocketService);

  toggleQueue(): void {
    if (this.isQueueing()) {
      this.cancelQueue();
    } else {
      this.joinQueue();
    }
  }

  // ── Entrar na fila ────────────────────────────────────────────────

  private joinQueue(): void {
    this.errorMessage.set(null);

    this.matchmakingService.joinQueue().subscribe({
      next: (event: MatchmakingEvent) => {
        if (event.type === 'ERROR') {
          this.errorMessage.set(event.message);
          return;
        }

        // Backend confirmou QUEUED — iniciar UI de espera e subscrever WS.
        this.isQueueing.set(true);
        this.startQueueTimer();
        this.subscribeToMatchmaking();
      },
      error: (err: unknown) => {
        console.error('Failed to join queue:', err);
        this.errorMessage.set('Failed to join queue. Please try again.');
      },
    });
  }

  // ── Sair da fila ──────────────────────────────────────────────────

  private cancelQueue(): void {
    this.matchmakingService.leaveQueue().subscribe({
      next: () => this.resetQueueState(),
      error: (err: unknown) => {
        console.error('Failed to leave queue:', err);
        // Resetar mesmo em caso de erro para não bloquear a UI.
        this.resetQueueState();
      },
    });
  }

  // ── WebSocket: subscrever eventos de matchmaking ──────────────────

  private subscribeToMatchmaking(): void {
    // Limpar subscrição anterior, se existir.
    this.unsubscribeWs();

    this.wsSub = this.wsService.subscribe<MatchmakingEvent>('/user/queue/matchmaking').subscribe({
      next: (event: MatchmakingEvent) => {
        switch (event.type) {
          case 'MATCHED':
            // Match encontrado! Emitir para o pai e resetar.
            this.matchFound.emit(event);
            this.resetQueueState();
            break;

          case 'TIMEOUT':
          case 'CANCELLED':
          case 'ERROR':
            // Voltar ao estado inicial.
            this.resetQueueState();
            break;

          // QUEUED é informativo — já estamos a mostrar a UI de espera.
          default:
            break;
        }
      },
    });
  }

  // ── Timer visual de espera ────────────────────────────────────────

  private startQueueTimer(): void {
    let seconds = 0;
    this.queueInterval = setInterval(() => {
      seconds++;
      const mins = Math.floor(seconds / 60).toString().padStart(2, '0');
      const secs = (seconds % 60).toString().padStart(2, '0');
      this.queueTime.set(`${mins}:${secs}`);
    }, 1000);
  }

  // ── Helpers ───────────────────────────────────────────────────────

  /** Repõe o componente no estado inicial (sem queue). */
  private resetQueueState(): void {
    this.isQueueing.set(false);
    this.clearQueueInterval();
    this.unsubscribeWs();
  }

  // Limpa o intervalo de queue para evitar memory leak.
  private clearQueueInterval(): void {
    if (this.queueInterval !== null) {
      clearInterval(this.queueInterval);
      this.queueInterval = null;
    }
    this.queueTime.set('00:00');
  }

  private unsubscribeWs(): void {
    if (this.wsSub) {
      this.wsSub.unsubscribe();
      this.wsSub = null;
    }
  }

  // Limpa recursos ao destruir o componente.
  ngOnDestroy(): void {
    this.clearQueueInterval();
    this.unsubscribeWs();
  }
}
