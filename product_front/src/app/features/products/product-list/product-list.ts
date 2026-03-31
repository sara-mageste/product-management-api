import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, HostListener} from '@angular/core';
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
  isFilterOpen = false;

  currentPage = 0;
  pageSize = 8;

  ignoreBlur = false;

  @ViewChild('searchContainer') searchContainer!: ElementRef;
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;
  @ViewChild('filterContainer') filterContainer!: ElementRef;

  constructor(
    private productService: ProductService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadProducts(0);
  }

  loadProducts(page: number = 0 ): void {
    this.currentPage = page;
    this.loadProductsWithFilters();
  }

  trackById(index: number, product: any): number {
    return product.id;
  }
  
  //Click Outside Global
  @HostListener('document:click', ['$event'])
  handleClickOutSide(event: MouseEvent): void{
    
    const target = event.target as HTMLElement;

    //Search
    if (
      this.isSearchOpen &&
      !this.searchContainer?.nativeElement.contains(target)
    ){
      this.isSearchOpen = false;
    }

    //Filter
    if (
      this.isFilterOpen &&
      !this.filterContainer?.nativeElement.contains(target)
    ){
      this.isFilterOpen = false;
    }

  }

  //SEARCH
  
  toggleSearch() : void{
    this.isSearchOpen = !this.isSearchOpen;

    if (this.isSearchOpen) {
      setTimeout(() => {
        this.searchInput?.nativeElement.focus();
      });
    } 
  }

  search(): void {
    this.currentPage = 0;
    this.loadProductsWithFilters();
  }

  //FILTER

  toggleFilter(): void{
    this.isFilterOpen = !this.isFilterOpen;
  }

  /* SORT BY */
  filters = {
    sortBy: null as string | null,
    status: null as 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK' | null,
    categories: [] as string[],
    pageSize: null as number | null
  };

  setSortBy (sortBy: string){
    if (this.filters.sortBy === sortBy){
      this.filters.sortBy = null;
    } else {
      this.filters.sortBy = sortBy;
    }

    this.loadProductsWithFilters();
  }

  setStatus (status: any){

    if(this.filters.status === status){
      this.filters.status = null;
    } else {
      this.filters.status = status;
    }

    // Rule: Disable quantity if OUT_OF_STOCK
    if (this.filters.status === 'OUT_OF_STOCK' && this.filters.sortBy?.includes ('quantity')){
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
    this.currentPage = 0;
    this.loadProductsWithFilters();
  }

  clearFilters(): void {
    this.filters.sortBy = null;
    this.filters.status = null;
    this.filters.categories = [];
    this.filters.pageSize = null;

    this.pageSize = 8;
    this.searchTerm = '';

    this.cdr.detectChanges();

    this.loadProducts(0);
  }

  //API CALL

  loadProductsWithFilters(): void {
    this.loading = true;

    const params = {
      search: this.searchTerm?.trim() || null,
      sortBy: this.filters.sortBy,
      status: this.filters.status,
      categories: this.filters.categories,
      page: this.currentPage,
      size: this.filters.pageSize || this.pageSize
  };

    this.productService.getProducts(params).subscribe({
      next: (response) => {
        this.products = response?.products ?? [];
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
