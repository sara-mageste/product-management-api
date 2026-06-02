import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-notification-details',
  templateUrl: './notification-details.html',
  styleUrls: ['./notification-details.css']
})
export class NotificationDetailsComponent {

  @Input() notification: any;

  @Output() goToProduct = new EventEmitter<any>();

  onGoToProduct(): void {
    this.goToProduct.emit(this.notification);
  }
}