import { Component, signal } from '@angular/core';
import { ProductListComponent } from './features/products/product-list/product-list';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ProductListComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly title = signal('product-manager-front');
}