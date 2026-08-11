import { Injectable, signal } from '@angular/core';
import { v4 as uuidv4 } from 'uuid';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  readonly notifications = signal<Notification[]>([]);

  show(type: Notification['type'], message: string): void {
    const notification: Notification = { id: uuidv4(), type, message };
    this.notifications.update((items) => [...items, notification]);
    window.setTimeout(() => this.dismiss(notification.id), 5000);
  }

  dismiss(id: string): void {
    this.notifications.update((items) => items.filter((item) => item.id !== id));
  }
}
