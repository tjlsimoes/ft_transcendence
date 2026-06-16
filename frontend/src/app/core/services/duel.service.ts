import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RunResult } from '../../arena-page/submission-result.model';

export interface SubmitPayload {
  code: string;
  language: string;
}

export interface RunCodePayload {
  code: string;
  language: string;
  stdin?: string;
}

interface RunCodeBackendResponse {
  status: string;
  headline: string;
  stdout: string | null;
  stderr: string | null;
  compilerMessage: string | null;
  executionTimeMs: number | null;
  expectedOutput: string | null;
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

  runCode(duelId: number, payload: RunCodePayload): Observable<RunResult> {
    return new Observable<RunResult>(subscriber => {
      this.http.post<RunCodeBackendResponse>(`${this.baseUrl}/${duelId}/run`, payload).subscribe({
        next: (resp) => {
          const result: RunResult = {
            status: resp.status as RunResult['status'],
            headline: resp.headline,
            stdout: resp.stdout ?? undefined,
            stderr: resp.stderr ?? undefined,
            compilerMessage: resp.compilerMessage ?? undefined,
            executionTimeMs: resp.executionTimeMs ?? undefined,
            expectedOutput: resp.expectedOutput ?? undefined,
          };
          subscriber.next(result);
          subscriber.complete();
        },
        error: (err) => subscriber.error(err),
      });
    });
  }

  getDuelStatus(duelId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${duelId}`);
  }

  /** Retorna o duel ativo (MATCHED ou IN_PROGRESS) do user autenticado, ou null se não houver. */
  getActiveDuel(): Observable<ActiveDuel | null> {
    return this.http.get<ActiveDuel | null>(`${this.baseUrl}/active`);
  }
}
