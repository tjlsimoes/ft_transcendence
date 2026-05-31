import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { DuelService } from '../services/duel.service';
import { catchError, map, of } from 'rxjs';

/**
 * Guard para todas as páginas protegidas fora da arena.
 *
 * Verifica, por ordem:
 * 1. O utilizador está autenticado — redireciona para /login se não estiver.
 * 2. O utilizador NÃO tem um duel ativo (MATCHED ou IN_PROGRESS).
 *    Se tiver, redireciona para /arena (sem params — a arena carrega do backend).
 *
 * Aplicado a: /lobby, /profile, /leaderboard.
 * Impede que um utilizador com duel ativo aceda a qualquer página fora da arena.
 */
export const lobbyGuard: CanActivateFn = (_route, _state) => {
  const authService = inject(AuthService);
  const duelService = inject(DuelService);
  const router = inject(Router);

  // 1. Autenticação
  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  // 2. Verificar se existe um duel ativo.
  return duelService.getActiveDuel().pipe(
    map((activeDuel) => {
      // Há um duel ativo → redirecionar para a arena
      console.warn('[LobbyGuard] User has active duel', activeDuel.duelId, '→ redirect to arena');
      return router.createUrlTree(['/arena']);
    }),
    catchError((err) => {
      if (err.status === 404) {
        // Nenhum duel ativo → permitir
        return of(true);
      }
      // Erro de rede/servidor → permitir por segurança
      console.warn('[LobbyGuard] Error checking active duel:', err.status, '— allowing');
      return of(true);
    })
  );
};
