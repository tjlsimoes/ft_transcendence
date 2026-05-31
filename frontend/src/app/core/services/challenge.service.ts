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

@Injectable({ providedIn: 'root' })
export class ChallengeService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/challenges`;

  getChallenge(id: number): Observable<ChallengeResponse> {
    return this.http.get<ChallengeResponse>(`${this.baseUrl}/${id}`);
  }
}
