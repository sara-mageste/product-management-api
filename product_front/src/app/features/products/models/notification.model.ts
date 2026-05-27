export interface Notification {

  id: number;
  title: string;
  message: string;
  type: 'LOW_STOCK' | 'PROMOTION';
  createdAt: string;
  productId: number;
  read: boolean;
  resolved: boolean;
}