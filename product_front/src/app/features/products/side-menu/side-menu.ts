import { Component, Input, Output, EventEmitter, ElementRef, ViewChild, HostListener, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { AboutMeComponent } from '../about-me/about-me';
import { NotificationsPopupComponent } from '../notifications/notifications-popup/notifications-popup';
import { Notification } from '../models/notification.model';
import { NotificationService } from '../service/notification.service';
import { NotificationDetailsModalComponent } from '../notifications/notifications-details-modal/notifications-details-modal';

@Component({
  selector: 'app-side-menu',
  standalone: true,
  templateUrl: './side-menu.html',
  styleUrls: ['./side-menu.css'],
  imports: [
    AboutMeComponent,
    CommonModule,
    NotificationsPopupComponent,
    NotificationDetailsModalComponent
  ]
})
export class SideMenuComponent implements OnInit, OnDestroy {

  @Input() isOpen = false;

  @Input() userProfile!: {
    name: string;
    employeeCode: string;
    imageUrl: string;
  };

  @Output() closeMenu = new EventEmitter<void>();
  @Output() openProductFromNotification = new EventEmitter<number>();
  @Output() unreadStateChange = new EventEmitter<boolean>();

  @ViewChild('menuContainer')
  menuContainer!: ElementRef;

  isAboutOpen = false;
  isNotificationsOpen = false;
  notificationsEnabled = true;
  hasUnreadNotifications = false;

  selectedNotification: Notification | null = null;
  isNotificationDetailsModalOpen = false;

  private notificationsSubscription?: Subscription;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    // atualiza o dot sempre que produto for salvo
    this.notificationsSubscription =
      this.notificationService.notificationsUpdated$.subscribe(() => {
        this.refreshUnreadState();
      });

    this.refreshUnreadState();
  }

  ngOnDestroy(): void {
    this.notificationsSubscription?.unsubscribe();
  }

  refreshUnreadState(): void {
    this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.hasUnreadNotifications = data.some(n => !n.read);
      },
      error: (err) => console.error('Error loading notifications', err)
    });
  }

  onNotificationsStateChange(hasUnread: boolean): void {
    this.hasUnreadNotifications = hasUnread;
    this.unreadStateChange.emit(hasUnread);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isOpen) return;
    const clickedInside = this.menuContainer.nativeElement.contains(event.target);
    if (!clickedInside) this.closeMenu.emit();
  }

  toggleAbout() {
    this.isAboutOpen = !this.isAboutOpen;
    if (this.isAboutOpen) this.isNotificationsOpen = false;
  }

  aboutMe = {
    name: 'Sara Mageste',
    employeeCode: 'EMP-2026',
    imageUrl: '/images/profile.png'
  };

  toggleNotifications() {
    this.isNotificationsOpen = !this.isNotificationsOpen;
    if (this.isNotificationsOpen) this.isAboutOpen = false;
  }

  openNotificationDetails(notification: Notification) {
    this.selectedNotification = notification;
    this.isNotificationDetailsModalOpen = true;
  }

  goToProductFromNotification(notification: Notification) {
    this.isNotificationDetailsModalOpen = false;
    this.isNotificationsOpen = false;
    this.openProductFromNotification.emit(notification.productId);
  }
}