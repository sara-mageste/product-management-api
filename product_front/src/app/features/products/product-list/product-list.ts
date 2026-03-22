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

  trackById(index: number, product: any): number {
    return product.id;
  }
  
  /* Opens and closes the search */
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;

  toggleSearch() : void{
    this.isSearchOpen = !this.isSearchOpen;

    if (this.isSearchOpen) {
      setTimeout(() => {
        this.searchInput?.nativeElement.focus();
      });
    } 
  }

  closeSearch(): void {
    if (this.ignoreBlur) {
      this.ignoreBlur = false;
      return;
    }

    this.isSearchOpen = false;
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

  /* SORT BY */
  filters = {
    sortBy: null as string | null,
    status: null as 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK' | null,
    categories: [] as string[],
    pageSize: null as number | null
  };

  setSortBy (sortBy: string){
    this.filters.sortBy = sortBy;
    this.loadProductsWithFilters();
  }

  setStatus (status: any){
    this.filters.status = status;

    // Rule: Disable quantity if OUT_OF_STOCK
    if (status === 'OUT_OF_STOCK' && this.filters.sortBy?.includes ('quantity')){
      this.filters.sortBy = null;
    }

    this.loadProductsWithFilters();
  }

  toggleCategory (category: string){
    const index = this.filters.categories.indexOf(category);

    if(index >= 0){
      this.filters.categories.splice(index,1);
    } else {
      this.filters.categories.push(category);
    }

    this.loadProductsWithFilters();
  }

  setPageSize(size: number){
    this.filters.pageSize = size;
    this.pageSize = size;
    this.loadProductsWithFilters();
  }

  loadProductsWithFilters(): void {
    this.loading = true;

    this.productService.getProductsWithFilters(
      this.buildQueryParams(),
      this.currentPage
    ).subscribe({
      next: (response) => {
        this.products = response.products ?? [];
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Error loading products';
        this.loading = false;
      }
    });
  }

  // Null Ignore
  buildQueryParams(): any{
    const params: any = {};

    if (this.filters.sortBy){
      params.sortBy = this.filters.sortBy;
    }

    if (this.filters.status){
      params.status = this.filters.status;
    }

    if (this.filters.categories.length > 0){
      params.categories = this.filters.categories;
    }

    if (this.filters.pageSize){
      params.size = this.filters.pageSize;
    }else{
      params.size = this.pageSize;
    }

    return params;
  }
}
