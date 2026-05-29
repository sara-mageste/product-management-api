import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { Notification } from '../../models/notification.model';
import { NotificationService } from '../../service/notification.service';

@Component({
  selector: 'app-notifications-popup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications-popup.html',
  styleUrls: ['./notifications-popup.css']
})
export class NotificationsPopupComponent implements OnInit, OnDestroy {

  @Input() isOpen = false;
  @Input() notifications: Notification[] = [];
  @Input() notificationsEnabled = true;
  @Output() close = new EventEmitter<void>();
  @Output() openNotificationDetails = new EventEmitter<any>();

  private notificationsSubscription?: Subscription;

  constructor(
    private notificationService: NotificationService
  ) {}

  // Lifecycle
  ngOnInit(): void {
    this.loadNotifications();

    this.notificationsSubscription =
    this.notificationService.notificationsUpdated$.subscribe(() => {
        this.loadNotifications();
      });
  }

  ngOnDestroy(): void {
    this.notificationsSubscription?.unsubscribe();
  }

  // Notifications
  loadNotifications(): void {

    this.notificationService.getNotifications().subscribe({
        next: (data) => {
          this.notifications = data;
        },
        error: (err) => {
          console.error('Error loading notifications',err);
        }
      });
  }

  toggleNotificationsEnabled() {
    this.notificationsEnabled = !this.notificationsEnabled;
  }

  markAsRead(notification: Notification): void {

    if (notification.read) {
      return;
    }

    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.map(item => {

            if (item.id === notification.id) {
              return {
                ...item,
                read: true
              };
            }

            return item;
          });

        this.notificationService.notifyNotificationsUpdated();
        },
        error: (err) => {
          console.error('Error marking notification as read',err);
        }

      });
  }

  openNotification(notification: any) {

    this.markAsRead(notification);
    this.openNotificationDetails.emit(notification);
  }

}