import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SubmitPayload {
  code: string;
  language: string;
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
}
