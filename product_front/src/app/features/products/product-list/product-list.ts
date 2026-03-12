import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from '../models/product.model';
import { ProductCardComponent } from '../product-card/product-card';
import { ProductService } from '../service/product.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductCardComponent],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css',
})
export class ProductListComponent implements OnInit {

  products: Product[] = [];

  loading = true;
  errorMessage = '';

  searchTerm = '';
  isSearchOpen = false; 

  currentPage = 0;
  pageSize = 8;

  ignoreBlur = false;

  constructor(
    private productService: ProductService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadProducts(0);
  }

  loadProducts(page: number = 0 ): void {
    this.loading = true;
    this.currentPage = page;

    this.productService.getProductsPaged(page, this.pageSize).subscribe({
      next: (response) => {
        this.products = response.products ?? [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Error loading products';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  /* Opens and closes the search */
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;

  toggleSearch() : void{
    this.isSearchOpen = !this.isSearchOpen;

    if (this.isSearchOpen) {
      setTimeout(() => {
        this.searchInput?.nativeElement.focus();
      });
    } else {
      this.searchTerm = '';
    }
  }

  closeSearch(): void {
    if (this.ignoreBlur) {
      this.ignoreBlur = false;
      return;
    }

    this.isSearchOpen = false;
    this.searchTerm = '';
    this.loadProducts(0);
  }

  /* GLOBAL search on the back-end */
  search(): void {
    const term = this.searchTerm.trim();

    if (!term) {
      this.loadProducts(0);
      return;
    }

    this.loading = true;
    this.currentPage = 0;

    this.productService.searchProducts(term, 0, this.pageSize).subscribe({
      next: (response) => {
        this.products = response.products ?? [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Error loading products';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

}
