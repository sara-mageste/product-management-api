import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation-modal.html',
  styleUrl: './confirmation-modal.css'
})
export class ConfirmationModalComponent {

  @Input() isOpen = false;
  @Input() title: string = 'Confirm action';
  @Input() message: string = 'Are you sure you want to proceed?';
  @Input() confirmText: string = 'Confirm';
  @Input() productName: string | null = null;

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

}