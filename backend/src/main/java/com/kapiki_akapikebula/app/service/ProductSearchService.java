package com.kapiki_akapikebula.app.service;

import com.kapiki_akapikebula.app.dto.ProductListingDto;
import com.kapiki_akapikebula.app.dto.ProductSearchResult;
import com.kapiki_akapikebula.app.model.Product;
import com.kapiki_akapikebula.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductRepository productRepository;

    // Only these field names are accepted for sorting — anything else defaults to "name"
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "brand", "price");

    public Page<ProductSearchResult> search(
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        // --- Step 1: Clean up inputs ---

        // Treat blank search term as "no filter" so the query skips it
        String keyword = (query == null || query.isBlank()) ? null : query.trim();

        // Reject unknown sort fields, fall back to "name"
        String rawSortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "name";

        // "price" lives on ShopProducts (alias sp), not Product (alias p),
        // so we tell SQL to look at sp.price specifically
        String sortField = "price".equals(rawSortField) ? "sp.price" : rawSortField;

        // Convert the "asc"/"desc" string into Spring's internal type
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // --- Step 2: Build pagination settings ---

        // PageRequest bundles together: which page, how many results, and sort order
        // Math.max/min guard against bad input (negative page, huge page size)
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 100),
                Sort.by(direction, sortField)
        );

        // --- Step 3: One SQL query — products + their shop listings together ---

        // JOIN FETCH in the repository means shop listings are loaded in the same
        // query, so no extra DB calls happen inside the loop below
        Page<Product> matchingProducts = productRepository.search(
                keyword, minPrice, maxPrice, pageable
        );

        // --- Step 4: Convert each Product entity into a ProductSearchResult DTO ---

        List<ProductSearchResult> results = matchingProducts.getContent().stream()
                .map(product -> {

                    // product.getShopProducts() does NOT trigger a new DB call here —
                    // the data was already loaded by JOIN FETCH above
                    List<ProductListingDto> listingDtos = product.getShopProducts().stream()
                            .sorted(Comparator.comparing(sp -> sp.getPrice()))
                            .map(sp -> new ProductListingDto(
                                    sp.getShop().getName(),
                                    sp.getPrice(),
                                    sp.getStockStatus(),
                                    sp.getProductUrl()
                            ))
                            .toList();

                    // List is sorted cheapest first, so index 0 is the lowest price
                    BigDecimal lowestPrice = listingDtos.isEmpty()
                            ? null
                            : listingDtos.get(0).price();

                    return new ProductSearchResult(
                            product.getId(),
                            product.getName(),
                            product.getBrand(),
                            product.getImageUrl(),
                            lowestPrice,
                            listingDtos
                    );
                })
                .toList();

        // Wrap results back into a Page so the response includes totalPages,
        // totalElements etc. — useful for the frontend to build pagination UI
        return new PageImpl<>(results, pageable, matchingProducts.getTotalElements());
    }
}