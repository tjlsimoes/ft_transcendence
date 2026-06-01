import { Injectable, signal } from "@angular/core";
import { ChatMessage, ConversationWindow } from "../../shared/models/chat.model";
import { FriendEntry } from "../../shared/models/user-profile.model";

const MAX_WINDOWS = 3;

@Injectable({ providedIn: 'root'})
export class ChatStateService {
    private _windows = signal<ConversationWindow[]>([]);
    readonly windows = this._windows.asReadonly();

    // Idempotent: clicking the same friend does nothing
    openConversation(friend: FriendEntry): void {
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
}