import { ProductStatus } from '../enums/product-status.enum';
import { ProductCategory } from '../enums/product-category.enum';

export interface Product {
  id?: number;
  name: string;
  description: string;
  price: number;
  discount?: number;
  quantity: number;
  category: ProductCategory | null;
  status: ProductStatus;
  imageUrl?: string | null;
  code: string;
}
