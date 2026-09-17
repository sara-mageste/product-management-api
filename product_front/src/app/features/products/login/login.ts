import { Component, signal, computed, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../service/auth.service';
import { RegisterModalComponent } from './register-modal/register-modal';
import { WelcomeModalComponent } from './welcome-modal/welcome-modal';
import { RegisterResponse } from '../models/auth.model';
import { ForgotPasswordModalComponent } from './forgot-password-modal/forgot-password-modal';

type Fruit = 'banana' | 'orange' | 'apple';

const MAX_ATTEMPTS = 3;
const ANIMATION_DURATION_MS = 650;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RegisterModalComponent, WelcomeModalComponent, ForgotPasswordModalComponent],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnDestroy {

  employeeCode = signal('');
  password = signal('');

  currentFruit = signal<Fruit>('banana');
  previousFruit = signal<Fruit | null>(null);
  isAnimating = signal(false);

  errorMessage = signal('');
  submitting = signal(false);
  isLocked = signal(false);

  showPassword = signal(false);
  passwordFocused = signal(false);

  showRegisterModal = signal(false);
  showWelcomeModal = signal(false);
  welcomeCode = signal('');

  showForgotPasswordModal = signal(false);

  private lockIntervalId?: ReturnType<typeof setInterval>;
  private animationTimeoutId?: ReturnType<typeof setTimeout>;

  stateClass = computed(() => `state-${this.currentFruit()}`);

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnDestroy(): void {
    if (this.lockIntervalId) clearInterval(this.lockIntervalId);
    if (this.animationTimeoutId) clearTimeout(this.animationTimeoutId);
  }

  fruitImage(fruit: Fruit): string {
    return `images/login/${fruit}.png`;
  }


  submit(): void {
    if (this.isLocked() || this.submitting()) return;

    const code = this.employeeCode().trim();
    const pass = this.password();

    if (!code || !pass) {
      this.errorMessage.set('Please fill in all fields.');
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');

    this.authService.login(code, pass).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/']);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.handleLoginError(err);
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword.update(v => !v);
  }

  private handleLoginError(err: HttpErrorResponse): void {
    const message: string = err.error?.message ?? 'Something went wrong. Please try again.';

    if (err.status === 423) {
      this.errorMessage.set(message);
      this.transitionFruit('apple');
      this.startLockCountdown(message);
      return;
    }

    this.errorMessage.set(message);

    const remainingMatch = message.match(/(\d+)\s+attempt\(s\)\s+remaining/i);

    if (remainingMatch) {
      const remaining = Number(remainingMatch[1]);
      const attemptsUsed = MAX_ATTEMPTS - remaining;
      this.transitionFruit(attemptsUsed >= 2 ? 'apple' : 'orange');
    }
  }

  private transitionFruit(target: Fruit): void {
    if (this.currentFruit() === target) return;

    this.previousFruit.set(this.currentFruit());
    this.currentFruit.set(target);
    this.isAnimating.set(true);

    if (this.animationTimeoutId) clearTimeout(this.animationTimeoutId);

    this.animationTimeoutId = setTimeout(() => {
      this.isAnimating.set(false);
      this.previousFruit.set(null);
    }, ANIMATION_DURATION_MS);
  }

  private startLockCountdown(message: string): void {
    this.isLocked.set(true);

    const totalSeconds = this.parseRemainingSeconds(message);

    if (this.lockIntervalId) clearInterval(this.lockIntervalId);

    if (totalSeconds == null) {
      return;
    }

    let remaining = totalSeconds;

    this.lockIntervalId = setInterval(() => {
      remaining -= 1;

      if (remaining <= 0) {
        this.unlockAccount();
        return;
      }

      this.errorMessage.set(this.buildLockMessage(remaining));
    }, 1000);
  }

  private parseRemainingSeconds(message: string): number | null {
    const withMinutes = message.match(/(\d+)m\s+(\d+)s/);
    if (withMinutes) {
      return Number(withMinutes[1]) * 60 + Number(withMinutes[2]);
    }

    const onlySeconds = message.match(/(\d+)s/);
    if (onlySeconds) {
      return Number(onlySeconds[1]);
    }

    return null;
  }

  private buildLockMessage(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;

    if (minutes > 0) {
      return `Account locked. Try again in ${minutes}m ${remainingSeconds}s.`;
    }

    return `Account locked. Try again in ${remainingSeconds}s.`;
  }

  private unlockAccount(): void {
    if (this.lockIntervalId) clearInterval(this.lockIntervalId);

    this.isLocked.set(false);
    this.errorMessage.set('');
    this.transitionFruit('banana');
  }

  openRegisterModal(): void {
    this.showRegisterModal.set(true);
  }

  closeRegisterModal(): void {
    this.showRegisterModal.set(false);
  }

  onRegistered(response: RegisterResponse): void {
    this.showRegisterModal.set(false);
    this.welcomeCode.set(response.employeeCode);
    this.showWelcomeModal.set(true);
  }

  onWelcomeConfirmed(): void {
    this.showWelcomeModal.set(false);
    this.employeeCode.set(this.welcomeCode());
  }
  
  openForgotPasswordModal(): void {
    this.showForgotPasswordModal.set(true);
  }

  closeForgotPasswordModal(): void {
    this.showForgotPasswordModal.set(false);
  }
  
}