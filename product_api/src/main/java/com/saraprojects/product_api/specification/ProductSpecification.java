package com.saraprojects.product_api.specification;

import com.saraprojects.product_api.enums.ProductCategory;
import com.saraprojects.product_api.enums.ProductStatus;
import com.saraprojects.product_api.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filter(
            String search,
            List<ProductCategory> categories,
            ProductStatus status
    ) {
        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            if (search != null && !search.isBlank()) {
                if (search.matches("\\d+")) {
                    predicates = cb.and(predicates,
                            cb.equal(root.get("code"), search));
                } else {
                    predicates = cb.and(predicates,
                            cb.like(
                                    cb.lower(root.get("name")),
                                    search.toLowerCase() + "%"
                            ));
                }
            }

            if (categories != null && !categories.isEmpty()) {
                predicates = cb.and(predicates,
                        root.get("category").in(categories));
            }

            if (status != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), status));
            }

            return predicates;
        };
    }
}