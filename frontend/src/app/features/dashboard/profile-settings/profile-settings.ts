import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { PLAYER_PROFILE_MOCK } from '../../../shared/models/profile.mock';
import type { PlayerProfile } from '../../../shared/models/profile.model';

// Página de configurações de perfil do jogador.
// Permite editar username, email, senha e foto de perfil.
@Component({
  selector: 'app-profile-settings',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './profile-settings.html',
  styleUrls: ['../../../auth/auth-shared.css', './profile-settings.css'],
})
export class ProfileSettings implements OnInit {
  // Dados do perfil carregados (mock → API futura).
  profile: PlayerProfile = PLAYER_PROFILE_MOCK;

  // Formulário de dados pessoais (username + email).
  personalForm!: FormGroup;
  // Formulário de alteração de senha.
  passwordForm!: FormGroup;

  // Preview local do avatar selecionado.
  avatarPreview = signal<string | null>(null);
  // Arquivo de avatar selecionado para upload.
  avatarFile = signal<File | null>(null);

  // Estados de loading por seção.
  savingPersonal = signal(false);
  savingPassword = signal(false);
  savingAvatar = signal(false);

  // Feedback visual (toast).
  toastMessage = signal('');
  toastType = signal<'success' | 'error'>('success');
  toastVisible = signal(false);

  // Toggle de visibilidade das senhas.
  showCurrentPassword = signal(false);
  showNewPassword = signal(false);
  showConfirmPassword = signal(false);

  // Confirmação de delete account.
  showDeleteConfirm = signal(false);

  constructor(
    private fb: FormBuilder,
    private titleService: Title,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.titleService.setTitle('Profile Settings — Code Arena');
    this.initForms();
  }

  // Inicializa os reactive forms com dados do perfil atual.
  private initForms(): void {
    this.personalForm = this.fb.group({
      username: [this.profile.username, [Validators.required, Validators.minLength(3)]],
      email: [this.profile.email, [Validators.required, Validators.email]],
    });

    this.passwordForm = this.fb.group(
      {
        currentPassword: ['', [Validators.required]],
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: this.passwordMatchValidator }
    );
  }

  // Validação de grupo: new password e confirm devem ser iguais.
  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const pw = control.get('newPassword')?.value;
    const confirm = control.get('confirmPassword')?.value;
    if (pw && confirm && pw !== confirm) return { passwordMismatch: true };
    return null;
  }

  // ── Avatar ──

  // Abre file picker ao clicar no avatar.
  triggerAvatarUpload(): void {
    const input = document.getElementById('avatar-input') as HTMLInputElement;
    input?.click();
  }

  // Processa arquivo selecionado: validação + preview.
  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    // Valida tipo.
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      this.showToast('Only JPG, PNG and WebP are allowed', 'error');
      return;
    }

    // Valida tamanho (max 2MB).
    if (file.size > 2 * 1024 * 1024) {
      this.showToast('Image must be under 2MB', 'error');
      return;
    }

    this.avatarFile.set(file);

    // Gera preview local.
    const reader = new FileReader();
    reader.onload = () => {
      this.avatarPreview.set(reader.result as string);
    };
    reader.readAsDataURL(file);
  }

  // Salva avatar (mock → API futura via FormData).
  async saveAvatar(): Promise<void> {
    if (!this.avatarFile()) return;

    this.savingAvatar.set(true);
    try {
      // TODO: integrar com API (enviar FormData com o arquivo).
      await this.simulateDelay(800);
      this.profile.avatarUrl = this.avatarPreview()!;
      this.avatarFile.set(null);
      this.showToast('Avatar updated successfully', 'success');
    } catch {
      this.showToast('Failed to update avatar', 'error');
    } finally {
      this.savingAvatar.set(false);
    }
  }

  // Remove preview sem salvar.
  cancelAvatar(): void {
    this.avatarPreview.set(null);
    this.avatarFile.set(null);
  }

  // ── Personal Info ──

  // Salva dados pessoais (username e/ou email).
  async savePersonalInfo(): Promise<void> {
    this.personalForm.markAllAsTouched();
    if (this.personalForm.invalid) return;

    this.savingPersonal.set(true);
    try {
      // TODO: integrar com API (PUT /api/profile).
      const payload = this.personalForm.value;
      await this.simulateDelay(600);
      this.profile.username = payload.username;
      this.profile.email = payload.email;
      this.showToast('Profile updated successfully', 'success');
    } catch {
      this.showToast('Failed to update profile', 'error');
    } finally {
      this.savingPersonal.set(false);
    }
  }

  // ── Password ──

  async savePassword(): Promise<void> {
    this.passwordForm.markAllAsTouched();
    if (this.passwordForm.invalid) return;

    this.savingPassword.set(true);
    try {
      // TODO: integrar com API (PUT /api/profile/password).
      await this.simulateDelay(800);
      this.passwordForm.reset();
      this.showToast('Password changed successfully', 'success');
    } catch {
      this.showToast('Failed to change password', 'error');
    } finally {
      this.savingPassword.set(false);
    }
  }

  toggleCurrentPassword(): void {
    this.showCurrentPassword.update(v => !v);
  }

  toggleNewPassword(): void {
    this.showNewPassword.update(v => !v);
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword.update(v => !v);
  }

  // ── Delete Account ──

  toggleDeleteConfirm(): void {
    this.showDeleteConfirm.update(v => !v);
  }

  async deleteAccount(): Promise<void> {
    // TODO: integrar com API (DELETE /api/profile).
    await this.simulateDelay(500);
    this.showToast('Account deletion requested', 'success');
    this.showDeleteConfirm.set(false);
  }

  // ── Helpers ──

  // Verifica se campo é inválido e foi tocado.
  hasError(form: FormGroup, field: string): boolean {
    const control = form.get(field);
    return !!(control && control.invalid && control.touched);
  }

  // Verifica mismatch de senha no form de password.
  hasPasswordMismatch(): boolean {
    const cp = this.passwordForm.get('confirmPassword');
    return !!(cp?.touched && this.passwordForm.errors?.['passwordMismatch']);
  }

  // Converte erros de validação em mensagens amigáveis.
  getErrorMessage(form: FormGroup, field: string): string {
    const control = form.get(field);
    if (!control || !control.errors || !control.touched) return '';

    if (control.errors['required']) {
      const labels: Record<string, string> = {
        username: 'Username is required',
        email: 'Email is required',
        currentPassword: 'Current password is required',
        newPassword: 'New password is required',
        confirmPassword: 'Confirmation is required',
      };
      return labels[field] ?? 'Required field';
    }
    if (control.errors['email']) return 'Invalid email address';
    if (control.errors['minlength']) {
      const min = control.errors['minlength'].requiredLength;
      return `Minimum ${min} characters`;
    }
    return '';
  }

  // Formata data relativa (ex: "2 days ago").
  getRelativeTime(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    if (days === 0) return 'today';
    if (days === 1) return 'yesterday';
    if (days < 30) return `${days}d ago`;
    const months = Math.floor(days / 30);
    return `${months}mo ago`;
  }

  // Exibe toast com auto-dismiss.
  private showToast(message: string, type: 'success' | 'error'): void {
    this.toastMessage.set(message);
    this.toastType.set(type);
    this.toastVisible.set(true);
    setTimeout(() => this.toastVisible.set(false), 3500);
  }

  // Simula latência de rede (substituído por chamada HTTP real no futuro).
  private simulateDelay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // Fallback caso a imagem de avatar falhe ao carregar.
  onAvatarError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
  }
}
