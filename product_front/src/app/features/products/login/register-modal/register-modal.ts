import { Component, output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../service/auth.service';
import { RegisterResponse } from '../../models/auth.model';

const AVATAR_COUNT = 8;

@Component({
  selector: 'app-register-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register-modal.html',
  styleUrl: './register-modal.css'
})
export class RegisterModalComponent {

  closed = output<void>();
  registered = output<RegisterResponse>();

  name = signal('');
  email = signal('');
  password = signal('');
  confirmPassword = signal('');
  avatarId = signal(1);

  triedSave = signal(false);
  submitting = signal(false);
  emailTaken = signal(false);

  avatarImage = computed(() => `images/avatars/avatar${this.avatarId()}.png`);

  nameInvalid = computed(() => this.triedSave() && !this.name().trim());

  passwordHint = 'Password must be at least 8 characters.';

  showPassword = signal(false);
  showConfirmPassword = signal(false);
  passwordFocused = signal(false);
  confirmPasswordFocused = signal(false);

  emailInvalid = computed(() =>
    this.emailTaken() || (this.triedSave() && !this.isEmailValid())
  );

  passwordInvalid = computed(() =>
    this.triedSave() && this.password().length < 8
  );

  confirmPasswordInvalid = computed(() =>
    this.triedSave() && (!this.confirmPassword() || this.confirmPassword() !== this.password())
  );

  togglePasswordVisibility(): void {
    this.showPassword.update(v => !v);
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword.update(v => !v);
  }

  errorMessage = computed(() => {
    if (this.emailTaken()) {
        return 'There is already an account with this email.';
    }

    if (!this.triedSave()) {
        return this.passwordHint;
    }

    if (
        this.nameInvalid() ||
        (this.triedSave() && !this.isEmailValid()) ||
        this.passwordInvalid() ||
        this.confirmPasswordInvalid()
    ) {
        return 'Please fill in all fields correctly.';
    }

    return this.passwordHint;
    });

  constructor(private authService: AuthService) {}

  previousAvatar(): void {
    this.avatarId.update(id => (id === 1 ? AVATAR_COUNT : id - 1));
  }

  nextAvatar(): void {
    this.avatarId.update(id => (id === AVATAR_COUNT ? 1 : id + 1));
  }

  private isEmailValid(): boolean {
    const value = this.email().trim();
    if (!value) return false;
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  }

  cancel(): void {
    this.closed.emit();
  }

  submit(): void {
    this.triedSave.set(true);
    this.emailTaken.set(false);

    if (
      this.nameInvalid() ||
      !this.isEmailValid() ||
      this.passwordInvalid() ||
      this.confirmPasswordInvalid()
    ) {
      return;
    }

    this.submitting.set(true);

    this.authService.register({
      name: this.name().trim(),
      email: this.email().trim(),
      password: this.password(),
      confirmPassword: this.confirmPassword(),
      avatarId: this.avatarId()
    }).subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.registered.emit(response);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.handleError(err);
      }
    });
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === 409) {
      this.emailTaken.set(true);
    }
  }
}