import { DestroyRef, inject, Injectable, signal } from "@angular/core";
import { SocketState, WebSocketService } from "./websocket.service";
import { ChatStateService } from "./chat-state.service";
import { NotificationPayload } from "../../shared/models/notification.model";
import { filter, switchMap } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private wsService = inject(WebSocketService);
  private chatState = inject(ChatStateService);
  private destroyRef = inject(DestroyRef);

  private _notifications = signal<NotificationPayload[]>([]);
  readonly notifications = this._notifications.asReadonly();

  private initialized = false;

  init(): void {
    if (this.initialized) return ;
    this.initialized = true;
    this.wsService.state$
      .pipe(
        filter(s => s === SocketState.CONNECTED),
        switchMap(() =>
          this.wsService.subscribe<NotificationPayload>('/user/queue/notifications')
        ),
        takeUntilDestroyed(this.destroyRef)
    )
    .subscribe(n => this.handle(n));
  }

  private handle(n: NotificationPayload): void {
    if (n.type === 'CHAT_MESSAGE') {
      const msg = n.payload as { senderId: number };
      const alreadyOpen = this.chatState.windows().some(w => w.friend.id === msg.senderId);
      if (!alreadyOpen)
        this.chatState.incrementPending(msg.senderId);
    } else {
      this._notifications.update(list => [n, ...list]);
    }
  }
}