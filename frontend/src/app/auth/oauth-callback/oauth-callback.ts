import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-oauth-callback',
  imports: [RouterLink],
  templateUrl: './oauth-callback.html',
  styleUrls: ['../auth-shared.css', './oauth-callback.css'],
})
export class OAuthCallbackComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  isLoading = signal(true);
  errorMessage = signal('');

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const token = params['token'];
      const refreshToken = params['refreshToken'];
      const error = params['error'];

      if (error) {
        this.errorMessage.set(decodeURIComponent(error));
        this.isLoading.set(false);
        return;
      }

      if (token) {
        // Save the received access token to localStorage
        this.authService.saveToken({
          accessToken: token,
          refreshToken: refreshToken || '',
          tokenType: 'Bearer',
          expiresIn: 3600,
        });

        // Redirect to the dashboard/lobby
        this.router.navigate(['/lobby']);
      } else {
        this.errorMessage.set('No token received from OAuth provider.');
        this.isLoading.set(false);
      }
    });
  }
}
