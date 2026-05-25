import { Component, OnDestroy, output, signal, computed } from '@angular/core';

@Component({
  selector: 'app-arena-timer',
  standalone: true,
  templateUrl: './arena-timer.html',
  styleUrl: './arena-timer.css',
})
export class ArenaTimer implements OnDestroy {
  /** Emitted when the countdown reaches zero. Parent should auto-submit. */
  readonly timeUp = output<void>();

  /** Remaining seconds — driven by the frontend interval. */
  readonly remaining = signal<number>(0);

  /**
   * True when the opponent finished and we forced the clock to 1 minute.
   * Triggers the red pulsing style.
   */
  readonly isUrgent = signal<boolean>(false);

  /** MM:SS string derived from `remaining`. */
  readonly display = computed<string>(() => {
    const s = this.remaining();
    const mm = Math.floor(s / 60).toString().padStart(2, '0');
    const ss = (s % 60).toString().padStart(2, '0');
    return `${mm}:${ss}`;
  });

  private intervalId: ReturnType<typeof setInterval> | null = null;

  private startInterval(): void {
    if (this.intervalId !== null) return;
    this.intervalId = setInterval(() => {
      const next = this.remaining() - 1;
      if (next <= 0) {
        this.remaining.set(0);
        this.stop();
        this.timeUp.emit();
      } else {
        this.remaining.set(next);
      }
    }, 1000);
  }

  /**
   * Syncs the remaining time with the backend.
   * On the first call, sets the initial time and starts the countdown.
   * On subsequent calls, only corrects if the drift is more than 2 seconds.
   */
  sync(seconds: number): void {
    if (!this.intervalId) {
      this.remaining.set(seconds);
      this.startInterval();
      return;
    }
    const diff = Math.abs(this.remaining() - seconds);
    if (diff > 2) {
      this.remaining.set(seconds);
    }
  }

  /**
   * Call this when the opponent submits a correct solution.
   *
   * - If more than 60 s remain → forces the clock to exactly 60 s and marks
   *   the timer as urgent (turns red).
   * - If 60 s or fewer remain → the timer continues unchanged (already close
   *   to the end; no need to modify it), but still marks as urgent.
   */
  opponentFinished(): void {
    if (this.remaining() > 60) {
      this.remaining.set(60);
    }
    this.isUrgent.set(true);
  }

  private stop(): void {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  ngOnDestroy(): void {
    this.stop();
  }
}
