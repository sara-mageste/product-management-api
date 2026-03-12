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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    // Pagination
    public Map<String, Object> getPagedResponse(int page, int size, String sortBy) {
        String[] sortParams = sortBy.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Product> pageProducts = repository.findAll(pageable);

        List<ProductDTO> products = pageProducts
                .getContent()
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

    // Search products by name (with pagination and sorting)
    public Map<String, Object> searchProducts(String name, int page, int size, String sortBy) {
        String[] sortParams = sortBy.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Product> pageProducts = repository.findByNameStartingWithIgnoreCase(name, pageable);

        List<ProductDTO> products = pageProducts
                .getContent()
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

    // Return all products (without pagination)
    public List<ProductDTO> getAllProducts() {
        return repository.findAll().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
    }

    // Find product by ID
    public ProductDTO getProductById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        return new ProductDTO(product);
    }

    // Filter products
    public Map<String, Object> getProductsWithFilters(
            ProductCategory category,
            ProductStatus status,
            int page,
            int size,
            String sortBy
    ) {
        String[] sortParams = sortBy.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        Page<Product> pageProducts;

        if (category != null && status != null) {
            pageProducts = repository.findByCategoryAndStatus(category, status, pageable);
        } else {
            pageProducts = repository.findAll(pageable);
        }

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

    // Create new product
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = dto.toEntity();
        Product saved = repository.save(product);
        return new ProductDTO(saved);
    }

    // Update existing product
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setQuantity(dto.getQuantity());
        existing.setCategory(dto.getCategory());
        existing.setStatus(dto.getStatus());
        existing.setImageUrl(dto.getImageUrl());

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
