import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private apiUrl = 'http://localhost:8080/notifications';

  constructor(private http: HttpClient) {}

  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl).pipe(
      tap(() => console.log('Notifications refreshed'))
    );
  }

  private notificationsUpdated = new BehaviorSubject<void>(undefined);

  notificationsUpdated$ = this.notificationsUpdated.asObservable();

  notifyNotificationsUpdated(): void {
    this.notificationsUpdated.next();
  }

  markAsRead(id: number) {
    return this.http.put(
      `${this.apiUrl}/${id}/read`,
      {}
    );
  }

}