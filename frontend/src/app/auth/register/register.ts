import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

// Componente de cadastro: valida dados, confirma senha e envia formulário.
@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['../auth-shared.css', './register.css'],
})
export class RegisterComponent {
  // FormGroup principal com dados necessários para criação de conta.
  registerForm: FormGroup;
  // Estado de carregamento durante envio do formulário.
  isLoading = signal(false);
  // Estados de exibição para campos de senha.
  showPassword = signal(false);
  showConfirmPassword = signal(false);
  // Mensagem de erro retornada pelo servidor.
  serverError = signal('');

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
  ) {
    // Define controles, validações e validação de grupo (senha x confirmação).
    this.registerForm = this.fb.group(
      {
        username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required]],
        acceptTerms: [false, [Validators.requiredTrue]],
      },
      { validators: this.passwordMatchValidator }
    );
  }

  // Validação de grupo: garante que password e confirmPassword sejam iguais.
  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const pw = control.get('password')?.value;
    const confirm = control.get('confirmPassword')?.value;
    if (pw && confirm && pw !== confirm) return { passwordMismatch: true };
    return null;
  }

  // Alterna visibilidade da senha principal.
  togglePasswordVisibility(): void {
    this.showPassword.update((v) => !v);
  }

  // Alterna visibilidade da confirmação de senha.
  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword.update((v) => !v);
  }

  // Retorna true quando o campo é inválido e já foi tocado.
  hasError(field: string): boolean {
    const control = this.registerForm.get(field);
    return !!(control && control.invalid && control.touched);
  }

  // Indica erro específico de divergência entre senha e confirmação.
  hasPasswordMismatch(): boolean {
    const cp = this.registerForm.get('confirmPassword');
    return !!(cp?.touched && this.registerForm.errors?.['passwordMismatch']);
  }

  // Converte erros de validação em mensagens amigáveis por campo.
  getErrorMessage(field: string): string {
    const control = this.registerForm.get(field);
    if (!control || !control.errors || !control.touched) return '';

    if (control.errors['required']) {
      const labels: Record<string, string> = {
        username: 'Username is required',
        email: 'Email is required',
        password: 'Password is required',
        confirmPassword: 'Confirmation is required',
        acceptTerms: 'You must accept the terms to continue',
      };
      return labels[field] ?? 'Required field';
    }
    if (control.errors['email']) return 'Invalid email';
    if (control.errors['minlength']) {
      const min = control.errors['minlength'].requiredLength;
      return `Minimum ${min} characters`;
    }
    if (control.errors['maxlength']) {
      const max = control.errors['maxlength'].requiredLength;
      return `Maximum ${max} characters`;
    }
    if (control.errors['requiredTrue']) return 'You must accept the terms to continue';
    return '';
  }

  // Mensagem de sucesso após registro.
  successMessage = signal('');

  // Executa submit: valida formulário, envia para API e redireciona ao final.
  onSubmit(): void {
    this.registerForm.markAllAsTouched();
    if (this.registerForm.invalid) return;

    this.isLoading.set(true);
    this.serverError.set('');

    const { username, email, password } = this.registerForm.value;

    this.authService.register({ username, email, password }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set('Account created successfully! Redirecting to login...');
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err: HttpErrorResponse) => {
        this.serverError.set(this.authService.extractApiError(err));
        this.isLoading.set(false);
      },
    });
  }
}
