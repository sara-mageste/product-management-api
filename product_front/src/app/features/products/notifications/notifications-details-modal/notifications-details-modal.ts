import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NotificationDetailsComponent } from '../notification-details/notification-details';

@Component({
  selector: 'app-notification-details-modal',
   imports: [NotificationDetailsComponent],
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
