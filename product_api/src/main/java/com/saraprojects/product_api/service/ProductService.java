package com.saraprojects.product_api.service;

import com.saraprojects.product_api.enums.ProductCategory;
import com.saraprojects.product_api.enums.ProductStatus;
import com.saraprojects.product_api.dto.ProductDTO;
import com.saraprojects.product_api.model.Product;
import com.saraprojects.product_api.repository.ProductRepository;
import com.saraprojects.product_api.service.NotificationService;
import com.saraprojects.product_api.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final NotificationService notificationService;

    private Pageable buildPageable (int page, int size, String sortBy){
        try{
            String[] sortParams = sortBy.split(",");
            String sortField = sortParams[0];
            Sort.Direction sortDirection = Sort.Direction.ASC;

            if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
                sortDirection = Sort.Direction.DESC;
            }

            return PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        } catch (Exception e) {
            return PageRequest.of(page, size, Sort.by("name").ascending());
        }
    }

    private Map<String, Object> buildResponse (Page<Product> pageProducts, String sortBy){
        List<ProductDTO> products = pageProducts.getContent()
                .stream()
                .map(ProductDTO::new)
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("products", products);
        response.put("currentPage", pageProducts.getNumber());
        response.put("totalItems", pageProducts.getTotalElements());
        response.put("totalPages", pageProducts.getTotalPages());
        response.put("pageSize", pageProducts.getSize());
        response.put("sortBy", sortBy);

        return response;
    }
    //Unified Search + Filter + Pagination
    public Map<String, Object> getProducts(
            String search,
            List<ProductCategory> categories,
            ProductStatus status,
            int page,
            int size,
            String sortBy
    ){
        Pageable pageable = buildPageable(page, size, sortBy);

        Specification<Product> spec = ProductSpecification.filter(search, categories, status);
        Page<Product> pageProducts = repository.findAll(spec, pageable);

        return buildResponse(pageProducts, sortBy);
    }

    // Return all products (without pagination)
    public List<ProductDTO> getAllProducts() {
        return repository.findAll().stream()
                .map(ProductDTO::new)
                .toList();
    }

    // Find product by ID
    public ProductDTO getProductById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        return new ProductDTO(product);
    }

    // Create new product
    public ProductDTO createProduct(ProductDTO dto) {
        if (repository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Product code already exists");
        }

        Product product = dto.toEntity();
        Product saved = repository.save(product);

        if (saved.getQuantity() < 10) {
            notificationService.createLowStockNotification(saved);
        }

        return new ProductDTO(saved);
    }

    // Update existing product
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        if (!existing.getCode().equals(dto.getCode()) && repository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Product code already exists");
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setQuantity(dto.getQuantity());
        existing.setCategory(dto.getCategory());
        existing.setStatus(dto.getStatus());
        existing.setImageUrl(dto.getImageUrl());
        existing.setCode(dto.getCode());

        Product updated = repository.save(existing);

        if (updated.getQuantity() < 10) {
            notificationService.createLowStockNotification(updated);
        }

        return new ProductDTO(updated);
    }

    // Delete product
    public void deleteProduct(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Product not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    // Delete selected products
    public void deleteProducts(List<Long> ids) {
        repository.deleteAllById(ids);
    }
}
