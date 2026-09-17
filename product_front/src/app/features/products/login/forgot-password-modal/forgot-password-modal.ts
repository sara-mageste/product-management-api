import { Component, output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../service/auth.service';

type Step = 'request' | 'reset' | 'done';

@Component({
  selector: 'app-forgot-password-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password-modal.html',
  styleUrl: './forgot-password-modal.css'
})
export class ForgotPasswordModalComponent {

  closed = output<void>();

  step = signal<Step>('request');

  email = signal('');
  token = signal('');
  newPassword = signal('');
  confirmPassword = signal('');

  triedRequest = signal(false);
  triedReset = signal(false);
  submitting = signal(false);

  infoMessage = signal('');
  errorMessage = signal('');

  emailInvalid = computed(() => this.triedRequest() && !this.isEmailValid());
  tokenInvalid = computed(() => this.triedReset() && !this.token().trim());
  newPasswordInvalid = computed(() => this.triedReset() && this.newPassword().length < 8);
  confirmPasswordInvalid = computed(() =>
    this.triedReset() && (!this.confirmPassword() || this.confirmPassword() !== this.newPassword())
  );

  constructor(private authService: AuthService) {}

  cancel(): void {
    this.closed.emit();
  }

  private isEmailValid(): boolean {
    const value = this.email().trim();
    if (!value) return false;
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  }

  submitRequest(): void {
    this.triedRequest.set(true);
    this.errorMessage.set('');

    if (!this.isEmailValid()) {
      return;
    }

    this.submitting.set(true);

    this.authService.forgotPassword(this.email().trim()).subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.infoMessage.set(response.message);
        this.step.set('reset');
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('Something went wrong. Please try again.');
      }
    });
  }

  backToRequest(): void {
    this.step.set('request');
    this.errorMessage.set('');
    this.triedReset.set(false);
  }

  submitReset(): void {
    this.triedReset.set(true);
    this.errorMessage.set('');

    if (this.tokenInvalid() || this.newPasswordInvalid() || this.confirmPasswordInvalid()) {
      return;
    }

    this.submitting.set(true);

    this.authService.resetPassword(
      this.token().trim(),
      this.newPassword(),
      this.confirmPassword()
    ).subscribe({
      next: () => {
        this.submitting.set(false);
        this.step.set('done');
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(err.error?.message ?? 'Something went wrong. Please try again.');
      }
    });
  }
}