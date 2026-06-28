package com.kapiki_akapikebula.app.controller;

import com.kapiki_akapikebula.app.dto.ProductListingDto;
import org.springframework.web.bind.annotation.*;
import com.kapiki_akapikebula.app.service.ProductService;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
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
}
