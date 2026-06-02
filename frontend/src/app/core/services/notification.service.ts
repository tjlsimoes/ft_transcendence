import { DestroyRef, inject, Injectable, signal } from "@angular/core";
import { SocketState, WebSocketService } from "./websocket.service";
import { ChatStateService } from "./chat-state.service";
import { NotificationPayload } from "../../shared/models/notification.model";
import { filter, Subscription, switchMap } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { HttpClient } from "@angular/common/http";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private wsService = inject(WebSocketService);
  private chatState = inject(ChatStateService);
  private destroyRef = inject(DestroyRef);
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/notifications`

  private _notifications = signal<NotificationPayload[]>([]);
  readonly notifications = this._notifications.asReadonly();
  private _chatNotifIds = signal<Record<number, number[]>>({});

  private subscription?: Subscription;
  private initialized = false;

  init(): void {
    if (this.initialized) return ;
    this.initialized = true;
    this.loadUnread();  // Populate from DB on login
    this.subscription = this.wsService.state$
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
      if (!alreadyOpen) {
        this._chatNotifIds.update(map => ({
          ...map,
          [msg.senderId]: [...(map[msg.senderId] ?? []), n.id]
        }));
        this.chatState.incrementPending(msg.senderId);
      } else {
        this.markRead([n.id]);
      }
    } else {
      this._notifications.update(list => [n, ...list]);
    }
  }

  private loadUnread(): void {
    this.http.get<NotificationPayload[]>(`${this.baseUrl}/unread`)
      .subscribe(notifications => notifications.forEach(n => this.handle(n)));
  }

  markChatRead(senderId: number): void {
    const ids = this._chatNotifIds()[senderId] ?? [];
    this.markRead(ids);
    this._chatNotifIds.update(({ [senderId]: _, ...rest}) =>  rest);
  }

  markTabRead():void {
    const ids = this._notifications().map( n => n.id);
    this.markRead(ids);
  }

  private markRead(ids: number[]): void {
    ids.forEach(id =>
      this.http.patch(`${this.baseUrl}/${id}/read`, {}).subscribe()
    );
  }

  reset(): void {
    this.initialized = false;
    this.subscription?.unsubscribe();
    this.subscription = undefined;
    this._notifications.set([]);
    this._chatNotifIds.set({});
  }
}