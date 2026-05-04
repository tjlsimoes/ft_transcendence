import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

// Componente de login: valida formulário e controla fluxo de submissão.
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['../auth-shared.css', './login.css'],
})
export class LoginComponent {
  // FormGroup com os campos do formulário de login.
  loginForm: FormGroup;
  // Estado de loading para desabilitar botão e exibir spinner durante submit.
  isLoading = signal(false);
  // Controla exibição/ocultação da senha no input.
  showPassword = signal(false);
  // Mensagem de erro retornada pelo servidor.
  serverError = signal('');

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
  ) {
    // Estrutura e validações do formulário.
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      rememberMe: [false],
    });
  }

  // Alterna visibilidade da senha no campo password.
  togglePasswordVisibility(): void {
    this.showPassword.update((v) => !v);
  }

  // Retorna true quando o campo é inválido e já foi tocado pelo usuário.
  hasError(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control && control.invalid && control.touched);
  }

  // Mapeia erros de validação para mensagens amigáveis por campo.
  getErrorMessage(field: string): string {
    const control = this.loginForm.get(field);
    if (!control || !control.errors || !control.touched) return '';

    if (control.errors['required']) {
      return field === 'username' ? 'Username is required' : 'Password is required';
    }
    if (control.errors['minlength']) {
      const min = control.errors['minlength'].requiredLength;
      return `Minimum ${min} characters`;
    }

    return '';
  }

  // Executa submit: valida formulário, envia para API e navega após sucesso.
  onSubmit(): void {
    this.loginForm.markAllAsTouched();
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.serverError.set('');

    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe({
      next: (response) => {
        this.authService.saveToken(response);
        this.router.navigate(['/lobby']);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.serverError.set(this.authService.extractApiError(err));
        this.isLoading.set(false);
      },
    });
  }
}
