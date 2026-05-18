import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, HostListener} from '@angular/core'; 
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms'; 

import { Product } from '../models/product.model'; 
import { ProductCardComponent } from '../product-card/product-card'; 
import { ProductService } from '../service/product.service'; 

import { ProductStatus } from '../enums/product-status.enum'; 
import { ProductCategory } from '../enums/product-category.enum';

import { ProductModalComponent } from '../product-modal/product-modal';
import { DeleteConfirmModalComponent } from '../product-delete-modal/product-delete-modal';

import { SideMenuComponent } from '../side-menu/side-menu';

@Component({
  selector: 'app-product-list',
  standalone: true,
  
  imports: [
    CommonModule, 
    FormsModule, 
    ProductCardComponent, 
    ProductModalComponent, 
    DeleteConfirmModalComponent, 
    SideMenuComponent],

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

  showExitConfirm = false;
  showBulkDeleteConfirm = false;
  showDeleteSuccess = false;

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
    return product.id!;
  }
  
  //Click Outside Global
  @HostListener('document:click', ['$event'])
  
  handleClickOutSide(event: MouseEvent): void{
    
    const target = event.target as HTMLElement;

    if (target.closest('.modal-content')) {
      return;
    }

    if (
      this.isSearchOpen &&
      !this.searchContainer?.nativeElement.contains(target)
    ){
      this.isSearchOpen = false;
    }

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
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Error loading products';
        this.loading = false;
        this.cdr.markForCheck();
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

  //PopUp Product

  isDetailsOpen = false;
  isEditMode = false;
  triedSave = false;
  mode: 'view' | 'edit' | 'create' = 'view';
  showDeleteConfirm = false;
  showDiscountInfo = false;
  imageError = '';

  isDeleteMode = false;
  selectedProducts: number[] = [];

  selectedProduct: Product | null = null;
  editableProduct: Product | null = null;
  
  openCreateProduct() {
    this.editableProduct = {
      name: '',
      code: '',
      price: 0,
      category: null,
      quantity: 0,
      status: ProductStatus.ACTIVE,
      description: '',
      imageUrl: null
    };

    this.selectedProduct = null;
    this.isDetailsOpen = true;
    this.isEditMode = true;
    this.mode = 'create';
    this.triedSave = false;
    this.imageError = '';
  }
  
  openProductDetails(product: Product){

    this.imageError = '';

    this.productService.getProductById(product.id!).subscribe({
      next: (response) => {
        this.selectedProduct = response;
        this.editableProduct = {...response};
        this.isDetailsOpen = true;
        this.isEditMode = false;
        this.mode = 'view';
        this.cdr.detectChanges();
      }
    });

  }

  hasUnsavedChanges(): boolean {
    if (!this.editableProduct) return false;

    if (this.mode === 'create') {
      return !!(
        this.editableProduct.name ||
        this.editableProduct.code ||
        this.editableProduct.price ||
        this.editableProduct.category ||
        this.editableProduct.quantity ||
        this.editableProduct.description ||
        this.editableProduct.imageUrl
      );
    }

    if (this.mode === 'edit' || this.mode === 'view') {
      return JSON.stringify(this.editableProduct) !== JSON.stringify(this.selectedProduct);
    }

    return false;
  }

  enableEdit() {
    this.isEditMode = true;
    this.mode = 'edit';
  }

  isCodeInvalid(): boolean {

    const code = this.editableProduct?.code;

    if (!code) return false;

    const regex = /^\d{8,20}$/;

    return !regex.test(code);
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
      this.isCodeInvalid() ||
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

    if (this.mode === 'create') {
      this.productService.createProduct(this.editableProduct).subscribe({
        next: (newProduct) => {
          this.selectedProduct = newProduct;
          this.editableProduct = { ...newProduct };
          this.isEditMode = false;
          this.triedSave = false;
          this.closeDetails();
          this.loadProductsWithFilters();
        }
      });
    } else {
      this.productService.updateProduct(this.editableProduct).subscribe({
        next: (updatedProduct) => {
          this.selectedProduct = updatedProduct;
          this.editableProduct = { ...updatedProduct };
          this.isEditMode = false;
          this.triedSave = false;
          this.loadProductsWithFilters();
        }
      });
    }
  }

  toggleDiscountInfo(){
    this.showDiscountInfo = !this.showDiscountInfo;
  }

  closeDetails(){
    this.isDetailsOpen = false;
    this.selectedProduct = null;
    this.editableProduct = null;
    this.triedSave = false;
    this.mode = 'view';
    this.imageError = '';
  }

  handleCloseAttempt(){

    if (this.hasUnsavedChanges()) {
      this.showExitConfirm = true;
      return;
    }

    this.closeDetails();
  }

  confirmExit() {
  this.showExitConfirm = false;
  this.closeDetails();
  }

  cancelExit() {
    this.showExitConfirm = false;
  }

  // Delete Product
  deleteProduct() {
    this.showDeleteConfirm = true;
  }

  confirmDelete() {

    if (!this.selectedProduct) return;

    this.productService.deleteProduct(this.selectedProduct.id!).subscribe(() => {
      this.showDeleteConfirm = false;
      this.closeDetails();
      this.loadProductsWithFilters();
    });

  }

  cancelDelete() {
    this.showDeleteConfirm = false;
  }

  toggleDeleteMode() {

    this.isDeleteMode = !this.isDeleteMode;

    if (!this.isDeleteMode) {
      this.selectedProducts = [];
    }

  }

  toggleProductSelection(product: Product) {

    if (!product.id) return;

    const index = this.selectedProducts.indexOf(product.id);

    if (index > -1) {
      this.selectedProducts.splice(index, 1);
    } else {
      this.selectedProducts.push(product.id);
    }

  }

  isProductSelected(productId: number): boolean {
    return this.selectedProducts.includes(productId);
  }

  handleProductClick(product: Product) {

    if (this.isDeleteMode) {
      this.toggleProductSelection(product);
      return;
    }
    this.openProductDetails(product);
  }

  deleteSelectedProducts() {

    if (this.selectedProducts.length === 0) return;
    this.showBulkDeleteConfirm = true;
  }

  confirmBulkDelete() {

    this.productService.deleteProducts(this.selectedProducts)
      .subscribe({

        next: () => {
          this.showBulkDeleteConfirm = false;
          this.selectedProducts = [];
          this.isDeleteMode = false;
          this.loadProductsWithFilters();
          this.showDeleteSuccess = true;
          setTimeout(() => {
            this.showDeleteSuccess = false;
          }, 4000);

        },
        error: (err) => {
          console.error('Error deleting products', err);
          alert(err.error?.message || 'Error deleting products');
        }
      });
  }

  cancelBulkDelete() {
    this.showBulkDeleteConfirm = false;
  }

  // Image Update
  selectedImageFile: File | null = null;

  onImageSelected(event: Event) {

    this.imageError = '';
    
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file || !this.editableProduct) return;

    if (!file.type.startsWith('image/')) {
      this.imageError = 'Selected file must be an image.';
      return;
    }

    if (file.size > 5_000_000) { // 5MB
      this.imageError = 'Image must be smaller than 5MB.';
      return;
    }

    this.imageError = '';
    this.selectedImageFile = file;

    const reader = new FileReader;

    reader.onload = () => {
      const base64 = reader.result as string;

      //Update preview immediately
      this.editableProduct!.imageUrl = base64;
      
      if (this.selectedProduct) {
      this.selectedProduct.imageUrl = base64;
    }

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
      },
      error: (err) => console.error("Error removing image", err),
    });
  }

  // Side Menu

  isMenuOpen = false;

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  userProfile = {
    name: 'Sara Mageste',
    employeeCode: 'EMP-2026',
    imageUrl: '/images/profile.png'
  };
}
