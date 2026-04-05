import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';

export interface PagedProductResponse {
  products: Product[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
  pageSize: number;
  sortBy: string;
}

@Injectable({ providedIn: 'root' })
export class ProductService {

  private readonly API_URL = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  // GET - Products with filters
  getProducts(paramsObj: any): Observable<PagedProductResponse>{

    let params = new HttpParams();

    Object.keys(paramsObj).forEach(key => {
      const value = paramsObj[key];

      if (Array.isArray(value)) {
        value.forEach(v => {
          params = params.append(key, v);
        });
      } else if (value !== null && value !== undefined) {
        params = params.set(key, value);
      }
    });

    return this.http.get<PagedProductResponse> (this.API_URL, {params});
  }

  //GET - Product by ID (Popup details)
  getProductById(id: number): Observable<Product>{
    return this.http.get<Product>(`${this.API_URL}/${id}`);
  }

  // PUT - Update Product
  updateProduct(product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.API_URL}/${product.id}`, product);
  }

  // DELETE - Product
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);

  }

}
