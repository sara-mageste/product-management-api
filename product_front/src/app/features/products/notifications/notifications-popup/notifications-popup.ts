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
  @Output() close = new EventEmitter<void>();
  @Output() openNotificationDetails = new EventEmitter<any>();
  @Output() unreadStateChange = new EventEmitter<boolean>();

  notifications: Notification[] = [];
  loading: boolean = false;

  private notificationsSubscription?: Subscription;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadNotifications();

    // mantém o listener original — quando produto é salvo, atualiza na hora
    this.notificationsSubscription =
      this.notificationService.notificationsUpdated$.subscribe(() => {
        this.loadNotifications();
      });
  }

  ngOnDestroy(): void {
    this.notificationsSubscription?.unsubscribe();
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.unreadStateChange.emit(data.some(n => !n.read));
      },
      error: (err) => console.error('Error loading notifications', err)
    });
  }

  openNotification(notification: Notification) {
    this.markAsRead(notification);
    this.openNotificationDetails.emit(notification);
  }

  markAsRead(notification: Notification): void {
    if (notification.read) return;

    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.map(item =>
          item.id === notification.id ? { ...item, read: true } : item
        );
        this.unreadStateChange.emit(this.notifications.some(n => !n.read));
      },
      error: (err) => console.error('Error marking notification as read', err)
    });
  }

  markAllAsRead(): void {
    if (this.loading) return;
    this.loading = true;

    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications = this.notifications.map(n => ({ ...n, read: true }));
        this.loading = false;
        this.unreadStateChange.emit(false);
      },
      error: (err) => {
        console.error('Error marking all as read', err);
        this.loading = false;
      }
    });
  }
}