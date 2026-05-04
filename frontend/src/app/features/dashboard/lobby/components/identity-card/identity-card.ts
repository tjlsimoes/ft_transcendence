import { Component, input, computed, signal } from '@angular/core';

@Component({
  selector: 'app-identity-card',
  imports: [],
  templateUrl: './identity-card.html',
  styleUrl: './identity-card.css',
})
export class IdentityCard {
  username = input.required<string>();
  league = input.required<string>();
  avatarUrl = input<string>('');
  isOnline = input<boolean>(true);

  avatarFailed = signal(false);
  avatarLetter = computed(() => {
    const name = this.username();
    return name ? name.charAt(0).toUpperCase() : '?';
  });

  /** Fallback local caso a API de avatar externo falhe. */
  onAvatarError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    this.avatarFailed.set(true);
  }
}
