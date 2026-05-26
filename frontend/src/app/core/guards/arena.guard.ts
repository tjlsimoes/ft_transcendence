import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { DuelService } from '../services/duel.service';
import { catchError, map, of } from 'rxjs';

/**
 * Guard da página de arena.
 *
 * Verifica:
 * 1. O utilizador está autenticado.
 * 2. O utilizador tem um duel ativo (MATCHED ou IN_PROGRESS) no backend.
 *
 * Se não houver duel ativo, redireciona para o lobby.
 * O URL é simplesmente /arena — sem query params. Toda a informação
 * do duel (challengeId, opponent, etc.) vem do backend.
 */
export const arenaGuard: CanActivateFn = (_route, _state) => {
  const authService = inject(AuthService);
  const duelService = inject(DuelService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  return duelService.getActiveDuel().pipe(
    map(() => {
      // Duel ativo existe — permitir acesso à arena
      return true;
    }),
    catchError((err) => {
      if (err.status === 404) {
        // Sem duel ativo — o user não deveria estar na arena
        console.warn('[ArenaGuard] Sem duel ativo → redirect para lobby');
        return of(router.createUrlTree(['/lobby']));
      }
      // Erro de rede — permitir (backend protege via 403 nos endpoints)
      console.warn('[ArenaGuard] Erro a verificar duel ativo:', err.status);
      return of(true);
    })
  );
};
