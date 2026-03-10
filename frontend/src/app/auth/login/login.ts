import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';

// Componente de login: valida formulário e controla fluxo de submissão.
@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class LoginComponent {
  // FormGroup com os campos do formulário de login.
  loginForm: FormGroup;
  // Estado de loading para desabilitar botão e exibir spinner durante submit.
  isLoading = signal(false);
  // Controla exibição/ocultação da senha no input.
  showPassword = signal(false);
  // Mensagem de erro vinda do backend.
  errorMessage = signal('');

  constructor(private fb: FormBuilder, private router: Router, private authService: AuthService) {
    // Estrutura e validações do formulário.
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
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
    if (control.errors['minlength']) return `Minimum ${control.errors['minlength'].requiredLength} characters`;

    return '';
  }

  // Executa submit: chama AuthService e navega após sucesso.
  onSubmit(): void {
    this.loginForm.markAllAsTouched();
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    const { username, password } = this.loginForm.value;
    this.authService.login({ username, password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.errorMessage.set(err.error?.error ?? 'Login failed. Check your credentials.');
        this.isLoading.set(false);
      },
    });
  }
}
