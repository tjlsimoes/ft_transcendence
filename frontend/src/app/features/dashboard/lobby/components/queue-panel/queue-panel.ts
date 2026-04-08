import { Component, OnDestroy, signal } from '@angular/core';

@Component({
  selector: 'app-queue-panel',
  imports: [],
  templateUrl: './queue-panel.html',
  styleUrl: './queue-panel.css',
})
export class QueuePanel implements OnDestroy {
  readonly isQueueing = signal(false);
  readonly queueTime = signal('00:00');
  private queueInterval: ReturnType<typeof setInterval> | null = null;

  toggleQueue(): void {
    this.isQueueing.set(!this.isQueueing());
    this.isQueueing() ? this.startQueueTimer() : this.clearQueueInterval();
  }

  private startQueueTimer(): void {
    let seconds = 0;
    this.queueInterval = setInterval(() => {
      seconds++;
      const mins = Math.floor(seconds / 60).toString().padStart(2, '0');
      const secs = (seconds % 60).toString().padStart(2, '0');
      this.queueTime.set(`${mins}:${secs}`);
    }, 1000);
  }

  // Limpa o intervalo de queue para evitar memory leak.
  private clearQueueInterval(): void {
    if (this.queueInterval !== null) {
      clearInterval(this.queueInterval);
      this.queueInterval = null;
    }
    this.queueTime.set('00:00');
  }

  // Limpa recursos ao destruir o componente.
  ngOnDestroy(): void {
    this.clearQueueInterval();
  }
}
