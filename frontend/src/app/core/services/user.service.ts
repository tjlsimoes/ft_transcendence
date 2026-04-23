import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserProfile, MatchHistory, FriendEntry } from '../../shared/models/user-profile.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/users`;

  /** Estado partilhado do utilizador autenticado (null = ainda não carregado). */
  readonly currentUser = signal<UserProfile | null>(null);

  /** Atalhos derivados para uso direto em componentes. */
  readonly username = computed(() => this.currentUser()?.username ?? '...');
  readonly avatarLetter = computed(() => {
    const name = this.currentUser()?.username;
    return name ? name.charAt(0).toUpperCase() : '?';
  });
  readonly league = computed(() => this.currentUser()?.league ?? '...');

  /** Carrega o perfil do utilizador e atualiza o estado partilhado. */
  loadMe(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/me`).pipe(
      tap((user) => this.currentUser.set(user)),
    );
  }

  /** Carrega o histórico de partidas do utilizador autenticado. */
  loadMatches(): Observable<MatchHistory[]> {
    return this.http.get<MatchHistory[]>(`${this.baseUrl}/me/matches`);
  }

  /** Carrega a lista de amigos do utilizador autenticado. */
  loadFriends(): Observable<FriendEntry[]> {
    return this.http.get<FriendEntry[]>(`${this.baseUrl}/me/friends`);
  }

  /** Limpa o estado ao fazer logout. */
  clear(): void {
    this.currentUser.set(null);
  }
}
