import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SubmitPayload {
  code: string;
  language: string;
}

export interface ActiveDuel {
  duelId: number;
  challengeId: number;
  opponentId: number;
  opponentName: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class DuelService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/duels`;

  submitCode(duelId: number, payload: SubmitPayload): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/${duelId}/submit`, payload);
  }

  getDuelStatus(duelId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${duelId}`);
  }

  /** Retorna o duel ativo (MATCHED ou IN_PROGRESS) do user autenticado, ou null se não houver. */
  getActiveDuel(): Observable<ActiveDuel | null> {
    return this.http.get<ActiveDuel | null>(`${this.baseUrl}/active`);
  }
}
