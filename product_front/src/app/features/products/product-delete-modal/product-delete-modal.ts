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

  // Controle de abertura
  @Input() isOpen = false;

  // Conteúdo dinâmico
  @Input() title: string = 'Confirm action';
  @Input() message: string = 'Are you sure you want to proceed?';

  // Texto do botão principal
  @Input() confirmText: string = 'Confirm';

  // (Opcional) manter se quiser usar nome em algum caso específico
  @Input() productName: string | null = null;

  // Eventos
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

}