import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from '../models/product.model';
import { ProductStatus } from '../enums/product-status.enum';
import { ProductCategory } from '../enums/product-category.enum';
import { Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-product-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-modal.html',
  styleUrl: './product-modal.css',
})
export class ProductModalComponent {

  @Input() isDetailsOpen = false;
  @Input() editableProduct!: Product | null;
  @Input() isEditMode = false;
  @Input() triedSave = false;
  @Input() mode: 'view' | 'edit' | 'create' = 'view';
  @Input() imageError = '';
  @Input() showDiscountInfo = false;
  @Input() isCategoryOpen = false;
  @Output() enableEdit = new EventEmitter<void>();
  @Output() cancelEdit = new EventEmitter<void>();
  @Output() save = new EventEmitter<void>();
  @Output() close = new EventEmitter<void>();
  @Output() toggleStatus = new EventEmitter<any>();
  @Output() toggleDiscountInfo = new EventEmitter<void>();
  @Output() selectCategory = new EventEmitter<ProductCategory>(); 
  @Output() toggleCategoryDropdown = new EventEmitter<void>();
  @Output() deleteProductClick  = new EventEmitter<void>();
  @Output() removeImage = new EventEmitter<void>();
  @Output() imageSelected = new EventEmitter<Event>();
  @Output() closeAttempt = new EventEmitter<void>();

  ProductStatus = ProductStatus;
  ProductCategory = ProductCategory;

  
  isCodeInvalid(): boolean {

    const code = this.editableProduct?.code;

    if (!code) return false;

    const regex = /^\d{8,20}$/;

    return !regex.test(code);
  }

}