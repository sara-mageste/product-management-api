import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-welcome-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './welcome-modal.html',
  styleUrl: './welcome-modal.css'
})
export class WelcomeModalComponent {

  employeeCode = input.required<string>();

  confirmed = output<void>();

  onConfirm(): void {
    this.confirmed.emit();
  }
}