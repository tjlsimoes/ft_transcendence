export interface NotificationPayload {
  id: number;
  type: string;        // matches NotificationType enum — e.g. 'CHAT_MESSAGE', 'DUEL_RESULT'
  payload: unknown;    // the actual data — varies by type
  read: boolean;
  createdAt: string;
}
