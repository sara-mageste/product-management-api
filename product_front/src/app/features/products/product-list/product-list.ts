import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, HostListener} from '@angular/core'; 
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 

import { Product } from '../models/product.model'; 
import { ProductCardComponent } from '../product-card/product-card'; 
import { ProductService } from '../service/product.service'; 

import { ProductStatus } from '../enums/product-status.enum'; 
import { ProductCategory } from '../enums/product-category.enum';

import { ProductModalComponent } from '../product-modal/product-modal';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductCardComponent, ProductModalComponent],
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

  ProductCategory = ProductCategory;
  ProductStatus = ProductStatus;

  currentPage = 0;
  pageSize = 8;
  totalPages = 0;

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

  trackById(index: number, product: Product): number {
    return product.id;
  }
  
  //Click Outside Global
  @HostListener('document:click', ['$event'])
  handleClickOutSide(event: MouseEvent): void{
    
    const target = event.target as HTMLElement;

    if (target.closest('.modal-content')) {
      return;
    }

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
    status: null as ProductStatus | null,
    categories: [] as ProductCategory[],
    pageSize: null as number | null
  };

  setSortBy (sortBy: string){
    this.filters.sortBy = this.filters.sortBy === sortBy ? null : sortBy;
    this.loadProductsWithFilters();
  }

  setStatus (status: ProductStatus){

    this.filters.status = this.filters.status === status ? null : status;

    // Rule: Disable quantity if OUT_OF_STOCK
    if (this.filters.status === ProductStatus.OUT_OF_STOCK && this.filters.sortBy?.includes ('quantity')){
      this.filters.sortBy = null;
    }

    this.loadProductsWithFilters();
  }

  toggleCategory (category: ProductCategory){
    const index = this.filters.categories.indexOf(category);

    if(index >= 0){
      this.filters.categories.splice(index,1);
    } else {
      this.filters.categories.push(category);
    }

    this.loadProductsWithFilters();
  }

  isCategoryOpen = false;

  toggleCategoryDropdown(){
    this.isCategoryOpen = !this.isCategoryOpen
  }

  selectCategory(category: ProductCategory) {
    if (!this.editableProduct) return;

    this.editableProduct.category = category;
    this.isCategoryOpen = false;
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
        this.totalPages = response?.totalPages ?? 0;
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

  //Pagination
  nextPage(): void {
    if (this.currentPage < this.totalPages - 1){
      this.currentPage++;
      this.loadProductsWithFilters();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0){
      this.currentPage--;
      this.loadProductsWithFilters();
    }
  }

  //PopUp Product Details

  isDetailsOpen = false;
  isEditMode = false;
  triedSave = false;
  showDeleteConfirm = false;
  showDiscountInfo = false;

  selectedProduct: Product | null = null;
  editableProduct: Product | null = null;
  
  openProductDetails(product: Product){
    this.productService.getProductById(product.id).subscribe({
      next: (response) => {
        this.selectedProduct = response;
        this.editableProduct = {...response};
        this.isDetailsOpen = true;
        this.isEditMode = false;
        this.cdr.detectChanges();
      }
    });

  }

  enableEdit() {
    this.isEditMode = true;
  }

  toggleStatus(event:any) {
    if (!this.editableProduct) return;

    const checked = event.target.checked;

    if(checked){
      this.editableProduct.status = ProductStatus.ACTIVE;
    } else {
      this.editableProduct.status = ProductStatus.INACTIVE;
    }
  }

  cancelEdit() {
    if (!this.selectedProduct) return;

    this.editableProduct = {
      ...this.selectedProduct
    };

    this.isEditMode = false;
  }

  saveChanges() {
    if(!this.editableProduct) return;

    this.triedSave = true;

    //Required validation
    if (
      !this.editableProduct.name ||
      !this.editableProduct.code ||
      !this.editableProduct.price ||
      !this.editableProduct.category ||
      !this.editableProduct.quantity
    ){
      return;
    }

    //Rule quantity = 0 -> Status = OUT_OF_STOCK
    if (this.editableProduct.quantity === 0) {
      this.editableProduct.status = ProductStatus.OUT_OF_STOCK;
    }

    this.productService.updateProduct(this.editableProduct).subscribe ({
      next: (updateProduct) => {
      this.selectedProduct = updateProduct;
      this.editableProduct = {...updateProduct};
      this.isEditMode = false;
      this.triedSave = false;
      this.loadProductsWithFilters();
      }
    });
  }

  toggleDiscountInfo(){
    this.showDiscountInfo = !this.showDiscountInfo;
  }

  closeDetails(){
    this.isDetailsOpen = false;
    this.selectedProduct = null;
    this.editableProduct = null;
  }

  // Delete Product
  deleteProduct() {
    this.showDeleteConfirm = true;
  }

  confirmDelete() {

    if (!this.selectedProduct) return;

    this.productService.deleteProduct(this.selectedProduct.id).subscribe(() => {
      this.showDeleteConfirm = false;
      this.closeDetails();
      this.loadProductsWithFilters();
    });

  }

  cancelDelete() {
    this.showDeleteConfirm = false;
  }

  // Image Update
  selectedImageFile: File | null = null;

  onImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file || !this.editableProduct) return;

    if (!file.type.startsWith('image/')) {
      console.error("Selected file is not an image!");
      return;
    }

    if (file.size > 5_000_000) { // 5MB
      console.error("File too large! Max 5MB.");
      return;
    }

    this.selectedImageFile = file;

    const reader = new FileReader;

    reader.onload = () => {
      const base64 = reader.result as string;

      //Update preview immediately
      this.editableProduct!.imageUrl = base64;
      this.selectedProduct!.imageUrl = base64;
      this.cdr.detectChanges();
      this.saveImage(base64);
    };

    reader.readAsDataURL(file);
    input.value = '';
  }

  saveImage(base64: string) {
    if (!this.editableProduct) return;

    const productToUpdate = { ...this.editableProduct, imageUrl: base64 };

    this.productService.updateProduct(productToUpdate).subscribe({
      next: (updatedProduct) => {
        this.selectedProduct = {...updatedProduct};
        this.editableProduct = { ...updatedProduct};
        this.loadProductsWithFilters();
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Error updating image", err),
    });
  }

  removeImage(){
    if (!this.editableProduct) return;

    this.editableProduct.imageUrl = null;
    this.selectedProduct!.imageUrl = null;

    const productToUpdate = {...this.editableProduct, imageUrl: null};

    this.productService.updateProduct(productToUpdate).subscribe({
      next: (updatedProduct) => {
        this.selectedProduct = {...updatedProduct};
        this.editableProduct = { ...updatedProduct};
        this.loadProductsWithFilters();
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Error removing image", err),
    });
  }

}
