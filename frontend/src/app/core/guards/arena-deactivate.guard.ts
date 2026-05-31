import { CanDeactivateFn } from '@angular/router';
import { ArenaPage } from '../../arena-page/arena-page';

/**
 * Guard de saída da arena.
 *
 * Bloqueia TODA a navegação enquanto o duel estiver ativo (isDuelActive = true).
 * Isto inclui:
 *   - Botão Back do browser
 *   - Alteração manual do URL
 *   - Links internos (router.navigate)
 *
 * Quando o duel termina (COMPLETED, DRAW, TIMEOUT), o isDuelActive fica false
 * e a navegação é permitida livremente (ex: botão "Leave Arena" pós-resultado).
 */
export const arenaDeactivateGuard: CanDeactivateFn<ArenaPage> = (component) => {
  if (component.isDuelActive()) {
    // Duel em curso — bloquear navegação silenciosamente
    return false;
  }
  // Duel terminado — permitir saída
  return true;
};
