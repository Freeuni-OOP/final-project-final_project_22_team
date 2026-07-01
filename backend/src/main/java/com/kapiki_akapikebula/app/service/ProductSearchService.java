package com.kapiki_akapikebula.app.service;

import com.kapiki_akapikebula.app.dto.ProductListingDto;
import com.kapiki_akapikebula.app.dto.ProductSearchResult;
import com.kapiki_akapikebula.app.model.Product;
import com.kapiki_akapikebula.app.model.ShopProducts;
import com.kapiki_akapikebula.app.repository.ProductRepository;
import com.kapiki_akapikebula.app.repository.ShopProductsRepository;
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
    private final ShopProductsRepository shopProductsRepository;

    // Only these two field names are allowed for sorting.
    // This prevents a user from passing "DROP TABLE" as a sort field.
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "brand");

    public Page<ProductSearchResult> search(
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {

        // If the user sent an empty string, treat it as no search term
        String keyword = (query == null || query.isBlank()) ? null : query.trim();

        // If someone passes an invalid sort field, fall back to "name"
        String sortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "name";

        // Convert "asc"/"desc" string into Spring's Sort.Direction enum
        Sort.Direction direction;
        if ("desc".equalsIgnoreCase(sortDir)) direction = Sort.Direction.DESC;
        else direction = Sort.Direction.ASC;


        // Math.max and Math.min guard against negative pages or giant page sizes
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),       // page can't be negative
                Math.min(size, 100),     // max 100 results per page
                Sort.by(direction, sortField)
        );

        // Fetch matching products from the DB

        // This calls the @Query in ProductRepository
        Page<Product> matchingProducts = productRepository.search(
                keyword, minPrice, maxPrice, pageable
        );

        // For each product, fetch its shop listings and build the result

        List<ProductSearchResult> results = matchingProducts.getContent().stream()
                .map(product -> {

                    // Get all shops selling this product, cheapest first
                    List<ShopProducts> shopListings = shopProductsRepository.findByProductIdOrderByPriceAsc(product.getId());

                    // Convert each ShopProducts row into a ProductListingDto
                    List<ProductListingDto> listingDtos = shopListings.stream()
                            .map(listing -> new ProductListingDto(
                                    listing.getShop().getName(),
                                    listing.getPrice(),
                                    listing.getStockStatus(),
                                    listing.getProductUrl()
                            )).toList();

                    // Since listings are already sorted cheapest first,
                    // the first one has the lowest price
                    BigDecimal lowestPrice;
                    if (shopListings.isEmpty()) lowestPrice = null;
                    else lowestPrice = shopListings.get(0).getPrice();

                    // Bundle everything into one result object
                    return new ProductSearchResult(
                            product.getId(),
                            product.getName(),
                            product.getBrand(),
                            product.getImageUrl(),
                            lowestPrice,
                            listingDtos
                    );
                }).toList();

        // Sort by price manually
        if ("price".equalsIgnoreCase(sortBy)) {
            Comparator<ProductSearchResult> byLowestPrice = Comparator.comparing(
                    result -> result.lowestPrice() != null ? result.lowestPrice() : BigDecimal.ZERO
            );

            results = results.stream()
                    .sorted("desc".equalsIgnoreCase(sortDir) ? byLowestPrice.reversed() : byLowestPrice)
                    .toList();
        }

        // Wrap the list back into a Page object so the response includes
        // pagination info (totalPages, totalElements, etc.)
        return new PageImpl<>(results, pageable, matchingProducts.getTotalElements());
    }
}