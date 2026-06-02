import { FriendEntry } from "./user-profile.model";

export interface ChatMessage {
    id: number;
    senderId: number;
    recipientId: number;
    content: string;
    createdAt: string;
}

export interface ChatMessageRequest {
    recipientId: number;
    content: string;
}

export interface ConversationWindow {
    friend: FriendEntry;
    messages: ChatMessage[];
    unread: number;
    minimized: boolean;
}