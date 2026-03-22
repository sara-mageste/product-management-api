package com.saraprojects.product_api.service;

import com.saraprojects.product_api.domain.enums.ProductCategory;
import com.saraprojects.product_api.domain.enums.ProductStatus;
import com.saraprojects.product_api.dto.ProductDTO;
import com.saraprojects.product_api.model.Product;
import com.saraprojects.product_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

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

    // Pagination
    public Map<String, Object> getPagedResponse(int page, int size, String sortBy) {
        Pageable pageable = buildPageable(page, size, sortBy);
        Page<Product> pageProducts = repository.findAll(pageable);
        return buildResponse(pageProducts, sortBy);
    }

    // Search products by name or code(with pagination and sorting)
    public Map<String, Object> searchProducts(String term, int page, int size, String sortBy) {
        Pageable pageable = buildPageable(page, size, sortBy);
        Page<Product> pageProducts;

        if(term.matches("\\d+")){
            pageProducts = repository.findByCode(term, pageable);

        } else{
            pageProducts = repository.findByNameStartingWithIgnoreCase(term, pageable);
        }

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

    // Filter products
    public Map<String, Object> getProductsWithFilters(
            List<ProductCategory> categories,
            ProductStatus status,
            int page,
            int size,
            String sortBy
    ) {

        Pageable pageable = buildPageable(page, size, sortBy);
        Page<Product> pageProducts;

        boolean hasCategories = categories != null && !categories.isEmpty();
        boolean hasStatus = status != null;

        if (hasCategories && hasStatus) {
            pageProducts = repository.findByCategoryInAndStatus(categories, status, pageable);
        } else if (hasCategories) {
            pageProducts = repository.findByCategoryIn(categories, pageable);
        } else if (hasStatus) {
            pageProducts = repository.findByStatus(status, pageable);
        } else {
            pageProducts = repository.findAll(pageable);
        }

        return buildResponse(pageProducts, sortBy);
    }

    // Create new product
    public ProductDTO createProduct(ProductDTO dto) {
        if (repository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Product code already exists");
        }
        Product product = dto.toEntity();
        Product saved = repository.save(product);
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
        return new ProductDTO(updated);
    }

    // Delete product
    public void deleteProduct(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Product not found with ID: " + id);
        }
        repository.deleteById(id);
    }
}
