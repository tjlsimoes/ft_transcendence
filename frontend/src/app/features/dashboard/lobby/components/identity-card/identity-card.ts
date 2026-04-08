import { Component, input } from '@angular/core';

@Component({
  selector: 'app-identity-card',
  imports: [],
  templateUrl: './identity-card.html',
  styleUrl: './identity-card.css',
})
export class IdentityCard {
  username = input.required<string>();
  league = input.required<string>();
  avatarUrl = input.required<string>();
  isOnline = input<boolean>(true);

  /** Fallback local caso a API de avatar externo falhe. */
  onAvatarError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
  }
}
