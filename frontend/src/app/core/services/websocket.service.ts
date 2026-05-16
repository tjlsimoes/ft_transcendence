import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { BehaviorSubject, Observable, filter, first, switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Connection states for our service
 */
export enum SocketState {
  DISCONNECTED,
  CONNECTING,
  CONNECTED
}

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  private client: Client | null = null;
  private state = new BehaviorSubject<SocketState>(SocketState.DISCONNECTED);

  constructor() {}

  /**
   * Connects to the WebSocket broker using the provided JWT.
   * Uses native WebSocket (the backend does not enable SockJS fallback).
   */
  connect(token: string): void {
    if (this.client && this.client.active) return;

    this.state.next(SocketState.CONNECTING);

    // Build the absolute WS URL from the environment.
    // In production (behind nginx proxy), wsUrl is '/ws' so we build wss://host/ws.
    const wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const brokerURL = `${wsProtocol}://${window.location.host}${environment.wsUrl}`;

    this.client = new Client({
      brokerURL,

      // Pass the JWT in the STOMP CONNECT frame
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },

      // Automatic reconnect settings
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        this.state.next(SocketState.CONNECTED);
        console.log('Successfully connected to WebSocket');
      },

      onDisconnect: () => {
        this.state.next(SocketState.DISCONNECTED);
        console.log('Disconnected from WebSocket');
      },

      onStompError: (frame: any) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      },

      onWebSocketError: (event: any) => {
        console.error('WebSocket connection error:', event);
      }
    });

    this.client.activate();
  }

  /**
   * Closes the connection.
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.state.next(SocketState.DISCONNECTED);
    }
  }

  /**
   * Subscribes to a topic and returns an Observable of the messages.
   * Type T is the expected JSON structure.
   */
  subscribe<T>(topic: string): Observable<T> {
    return this.state.pipe(
      // Wait until we are actually CONNECTED
      filter(s => s === SocketState.CONNECTED),
      first(), // Take the first connected signal to initiate subscription
      switchMap(() => {
        return new Observable<T>(observer => {
          const subscription = this.client?.subscribe(topic, (message: IMessage) => {
            observer.next(JSON.parse(message.body) as T);
          });

          // Cleanup: unsubscribe from STOMP when the RxJS Observable is destroyed
          return () => subscription?.unsubscribe();
        });
      })
    );
  }

  /**
   * Sends a message to a destination (e.g., /app/chat/1)
   */
  publish(destination: string, body: any): void {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination,
        body: JSON.stringify(body)
      });
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
