import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ChallengeResponse {
  id: number;
  title: string;
  description: string;
  difficulty: string;
  timeLimitSecs: number;
}

export interface JudgeCaseResultResponse {
  index: number;
  passed: boolean;
  actualOutput: string;
  expectedOutput: string;
  error: string | null;
  runtimeMs: number;
}

export interface JudgeResponse {
  passed: boolean;
  totalTests: number;
  passedTests: number;
  runtimeMs: number;
  memoryKb: number;
  compilationError: string | null;
  results: JudgeCaseResultResponse[];
}

export interface ChallengeRunCodeRequest {
  code: string;
  language: string;
}

@Injectable({ providedIn: 'root' })
export class ChallengeService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/challenges`;

  getChallenge(id: number): Observable<ChallengeResponse> {
    return this.http.get<ChallengeResponse>(`${this.baseUrl}/${id}`);
  }

  runCode(challengeId: number, payload: ChallengeRunCodeRequest): Observable<JudgeResponse> {
    return this.http.post<JudgeResponse>(`${this.baseUrl}/${challengeId}/run-code`, payload);
  }
}
