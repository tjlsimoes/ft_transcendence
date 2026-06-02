import { Injectable, signal } from "@angular/core";
import { ChatMessage, ConversationWindow } from "../../shared/models/chat.model";
import { FriendEntry } from "../../shared/models/user-profile.model";

const MAX_WINDOWS = 3;

@Injectable({ providedIn: 'root'})
export class ChatStateService {
    private _windows = signal<ConversationWindow[]>([]);
    private _pendingUnread = signal<Record<number, number>>({});
    readonly pendingUnread = this._pendingUnread.asReadonly();
    readonly windows = this._windows.asReadonly();

    // Idempotent: clicking the same friend does nothing
    openConversation(friend: FriendEntry): void {
        this.clearPending(friend.id);
        const existing = this._windows().find(w => w.friend.id === friend.id);

        if (existing) {
            // Already open, just un-minimize it
            this._windows.update(windows => windows.map(w => w.friend.id === friend.id ? {...w, minimized: false} : w));
            return;
        }

        // Enforce 3-window cap: drop oldest window if at max capacity
        this._windows.update(windows => {
            const trimmed = windows.length >= MAX_WINDOWS ? windows.slice(1): windows;
            return [...trimmed, { friend, messages: [], unread: 0, minimized: false}]
        });
    }

    closeConversation(friendId: number): void {
        this._windows.update(windows => windows.filter(w => w.friend.id !== friendId));
    }

    toggleMinimize(friendId: number): void {
        this._windows.update(windows => windows.map(w => w.friend.id === friendId ? {...w, minimized: !w.minimized} : w));
    }

    // Real-time message arrives via WebSocket
    addMessage(friendId: number, msg: ChatMessage): void {
        this._windows.update(windows =>
            windows.map(w => w.friend.id === friendId
                        ? {...w, messages: [...w.messages, msg]}
                        : w
                    )
        );
    }

    // When history loads
    prependHistory(friendId: number, msgs: ChatMessage[]): void {
        const chronological = [...msgs].reverse();
        this._windows.update(windows =>
                windows.map(w => w.friend.id === friendId
                            ? {...w, messages: [...chronological, ...w.messages]}
                            : w
                        )
            );
    }

    // Reset unread counter when user opens/focuses the window
    markRead(friendId: number): void {
    this._windows.update(windows =>
            windows.map(w => w.friend.id === friendId
                        ? {...w, unread: 0}
                        : w
                    )
        );
    }

    clear(): void {
        this._pendingUnread.set({});
        this._windows.set([]);
    }

    // Called when real-time message arrives for a minimized window
    incrementUnread(friendId: number): void {
        this._windows.update(windows =>
            windows.map(w => w.friend.id === friendId
                        ? {...w, unread: w.unread + 1}
                        : w
            )
        );
    }

    incrementPending(friendId: number): void {
        this._pendingUnread.update(map => ({
            ...map,
            [friendId]: (map[friendId] ?? 0) + 1
        }));
    }

    clearPending(friendId: number): void {
        const { [friendId]: _, ...rest } = this._pendingUnread();
        this._pendingUnread.set(rest);
    }

    syncFriendStatuses(friends: FriendEntry[]): void {
        this._windows.update(windows =>
            windows.map(w => {
                const updated = friends.find(f => f.id === w.friend.id);
                return updated ? {...w, friend: updated} : w;
            })
        )
    }

}