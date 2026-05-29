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

    @ViewChild('menuContainer') 
    menuContainer!: ElementRef;

    isAboutOpen = false;

    isNotificationsOpen = false;
    notificationsEnabled = true;
    hasUnreadNotifications = false;

    notifications: Notification[] = [];
    selectedNotification: Notification | null = null;
    isNotificationDetailsModalOpen = false;

    private notificationsSubscription?: Subscription;

    constructor(
        private notificationService: NotificationService
    ) {}

    // Lifecycle
    ngOnInit(): void {
        this.loadNotifications();

        this.notificationsSubscription =
        this.notificationService.notificationsUpdated$
        .subscribe(() => {
            this.checkUnreadNotifications();
        });
    }

    ngOnDestroy(): void {
        this.notificationsSubscription?.unsubscribe();
    }

    // Click Outside
    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {

        if (!this.isOpen) return;

        const clickedInside =
            this.menuContainer.nativeElement.contains(event.target);

        if (!clickedInside) {
            this.closeMenu.emit();
        }
    }

    // About
    toggleAbout() {
        this.isAboutOpen = !this.isAboutOpen;
    }

    aboutMe = {
        name: 'Sara Mageste',
        employeeCode: 'EMP-2026',
        imageUrl: '/images/profile.png'
    };
        

    // Notifications
    loadNotifications(): void {
        this.notificationService.getNotifications().subscribe({
            next: (data) => {
                this.notifications = data;
                this.hasUnreadNotifications = data.some(notification => !notification.read);
            },
            error: (err) => {
                console.error('Error loading notifications', err);
            }
        });
    }

    checkUnreadNotifications(): void {
        this.notificationService.getNotifications().subscribe({
            next: (notifications) => {
                this.hasUnreadNotifications = notifications.some(notification => !notification.read);
            },
                error: (err) => { console.error( 'Error checking notifications', err);
            }
        });
    }

    toggleNotifications() {
        this.isNotificationsOpen =
            !this.isNotificationsOpen;
    }

    toggleNotificationsEnabled() {
        this.notificationsEnabled =
            !this.notificationsEnabled;
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