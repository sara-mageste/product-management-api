import { Routes } from '@angular/router';
import { ProductListComponent } from './features/products/product-list/product-list';
import { NotificationsHistoryComponent } from './features/products/notifications/notifications-history/notifications-history';

export const routes: Routes = [
    {
        path: '',
        component: ProductListComponent
    },
    {
        path: 'notifications',
        component: NotificationsHistoryComponent
    }
];