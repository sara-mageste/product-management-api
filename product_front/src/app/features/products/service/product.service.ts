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

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly API_URL = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  getProductsPaged(
    page: number = 0,
    size: number = 8,
    sortBy: string = 'id,asc'
  ): Observable<PagedProductResponse> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy); 
    return this.http.get<PagedProductResponse>(
      `${this.API_URL}/paged`,
      { params }
    );
  }

  searchProducts(
    name: string,
    page: number = 0,
    size: number = 5,
    sortBy: string = 'id,asc'
  ): Observable<PagedProductResponse> {

    const params = new HttpParams()
      .set('name', name)
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy);

    return this.http.get<PagedProductResponse>(
      `${this.API_URL}/search`,
      { params }
    );
  }

  getProductsWithFilters (filters: any, page: number){
    let params = new HttpParams().set('page', String(page));

    if (filters.sortBy) params = params.set ('sortBy', filters.sortBy);
    if (filters.status) params = params.set ('status', filters.status);
    if (filters.size) params = params.set ('size', filters.size);

    if (filters.categories && filters.categories.length > 0){
      filters.categories.forEach((cat: string) => {
        params = params.append('categories', cat);
      });
    }

    return this.http.get<any>(`${this.API_URL}/filter`, {params});
  }

}
