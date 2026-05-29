import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-notification-details-modal',
  templateUrl: './notifications-details-modal.html',
  styleUrls: ['./notifications-details-modal.css']
})

export class NotificationDetailsModalComponent {

  @Input() notification: any;
  @Output() close = new EventEmitter<void>();
  @Output() goToProduct = new EventEmitter<any>();

  onGoToProduct() {
    this.goToProduct.emit(this.notification);
  }

}