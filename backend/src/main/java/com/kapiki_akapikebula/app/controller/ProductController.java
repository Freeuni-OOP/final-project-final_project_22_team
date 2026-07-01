package com.kapiki_akapikebula.app.controller;

import com.kapiki_akapikebula.app.dto.ProductListingDto;
import com.kapiki_akapikebula.app.dto.ProductSearchResult;
import com.kapiki_akapikebula.app.service.ProductSearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import com.kapiki_akapikebula.app.service.ProductService;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    public ProductController(ProductService productService, ProductSearchService productSearchService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
    }

    @GetMapping("/{id}/listings")
    public ResponseEntity<?> getProductListings(@PathVariable long id) {
        try {
            List<ProductListingDto> listings = productService.getProductListings(id);
            return ResponseEntity.ok(listings);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred while fetching listings.");
        }    
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductSearchResult>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductSearchResult> results = productSearchService.search(
                query, minPrice, maxPrice, sortBy, sortDir, page, size
        );
        return ResponseEntity.ok(results);
    }
}
