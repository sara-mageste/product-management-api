import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductCardComponent } from './product-card';

describe('ProductCardComponent', () => {
  let component: ProductCardComponent;
  let fixture: ComponentFixture<ProductCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductCardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductCardComponent);
    component = fixture.componentInstance;

    component.product = {
      id: 1,
      code: '109834761',
      name: 'Test Product',
      description: 'Test Description',
      price: 10,
      quantity: 5,
      imageUrl: 'https://via.placeholder.com/150'
    };

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it ('should display the product name', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect (compiled.textContent).toContain('Test Description');
  })

  it('should display the product description', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect (compiled.textContent).toContain('Test Description');    
  });

  it('should display the product price', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect (compiled.textContent).toContain('10');    
  });
});
