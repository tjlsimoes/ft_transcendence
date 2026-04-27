import { Component, OnInit, OnDestroy, output, signal, computed } from '@angular/core';

// ── Mock ──────────────────────────────────────────────────────────────────────
/**
 * Simulates fetching the total match duration from the backend.
 * Replace with a real HTTP call when the backend is ready.
 */
export function mockGetMatchDurationSeconds(): number {
  return 5 * 60; // 5 minutes
}
// ─────────────────────────────────────────────────────────────────────────────

@Component({
  selector: 'app-arena-timer',
  standalone: true,
  templateUrl: './arena-timer.html',
  styleUrl: './arena-timer.css',
})
export class ArenaTimer implements OnInit, OnDestroy {
  /** Emitted when the countdown reaches zero. Parent should auto-submit. */
  readonly timeUp = output<void>();

  private totalSeconds = mockGetMatchDurationSeconds();

  /** Remaining seconds — driven by the frontend interval. */
  readonly remaining = signal<number>(this.totalSeconds);

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

  ngOnInit(): void {
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
   * Call this when the opponent submits a correct solution.
   *
   * - If more than 60 s remain → forces the clock to exactly 60 s and marks
   *   the timer as urgent (turns red).
   * - If 60 s or fewer remain → the timer continues unchanged (already close
   *   to the end; no need to modify it).
   */
  opponentFinished(): void {
    if (this.remaining() > 60) {
      this.remaining.set(60);
      this.isUrgent.set(true);
    }
    // ≤ 60 s: do nothing — timer runs normally to zero
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
