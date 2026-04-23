import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-delete-confirm-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-delete-modal.html',
  styleUrl: './product-delete-modal.css'
})
export class DeleteConfirmModalComponent {

  @Input() isOpen = false;
  @Input() productName: string | null = null;

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}