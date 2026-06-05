import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { Router } from '@angular/router';

import { Notification } from '../../models/notification.model';
import { NotificationService } from '../../service/notification.service';

import { NotificationDetailsComponent } from '../notification-details/notification-details';
import { ConfirmationModalComponent } from '../../confirmation-modal/confirmation-modal';

@Component({
  selector: 'app-notifications-history',
  standalone: true,
  imports: [
    CommonModule,
    NotificationDetailsComponent,
    ConfirmationModalComponent
  ],
  templateUrl: './notifications-history.html',
  styleUrls: ['./notifications-history.css']
})
export class NotificationsHistoryComponent implements OnInit {

  notifications: Notification[] = [];
  pagedNotifications: Notification[] = [];
  loading = false;

  currentPage = 0;
  pageSize = 8;
  totalPages = 0;

  showClearHistoryConfirm = false;

  constructor(
    private notificationService: NotificationService,
    private location: Location,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading = true;

    this.notificationService.getAllNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.totalPages = Math.ceil(data.length / this.pageSize);
        this.updatePage();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading notifications', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  updatePage(): void {
    const start = this.currentPage * this.pageSize;
    this.pagedNotifications = this.notifications.slice(start, start + this.pageSize);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.updatePage();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.updatePage();
    }
  }

  goBack(): void {
    this.location.back();
  }

  onNotificationAction(notification: Notification): void {
    this.router.navigate(['/'], { queryParams: { openProduct: notification.productId } });
  }

  openClearConfirm(): void {
    this.showClearHistoryConfirm = true;
  }

  confirmClearHistory(): void {
    this.notificationService.clearHistory().subscribe({
      next: () => {
        this.notifications = [];
        this.pagedNotifications = [];
        this.totalPages = 0;
        this.currentPage = 0;
        this.showClearHistoryConfirm = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error clearing notifications history', err);
        this.showClearHistoryConfirm = false;
        this.cdr.detectChanges();
      }
    });
  }

  cancelClearHistory(): void {
    this.showClearHistoryConfirm = false;
  }
}