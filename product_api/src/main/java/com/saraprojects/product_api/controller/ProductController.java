package com.saraprojects.product_api.controller;

import com.saraprojects.product_api.domain.enums.ProductCategory;
import com.saraprojects.product_api.domain.enums.ProductStatus;
import com.saraprojects.product_api.dto.ProductDTO;
import com.saraprojects.product_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    //create Product
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(service.createProduct(dto));
    }

    //Update Product
    @PutMapping("/id/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(service.updateProduct(id, dto));
    }

    //Delete Product
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    //Delete Selected Products
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Void> deleteProducts(@RequestBody List<Long> ids) {
        service.deleteProducts(ids);
        return ResponseEntity.noContent().build();
    }

    //Get products by ID
    @GetMapping("/id/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    //Get products (Search+Filter+Pagination+Sort)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<ProductCategory> categories,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "name,asc") String sortBy
    ) {
        return ResponseEntity.ok(service.getProducts(search, categories, status, page, size, sortBy));
    }

    // Get all products
    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(service.getAllProducts());
    }
}
