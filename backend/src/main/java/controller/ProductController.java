package controller;



import com.kapiki_akapikebula.app.dto.ProductListingDto;
import org.springframework.web.bind.annotation.*;
import service.ProductService;

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
    public List<ProductListingDto> getProductListings(@PathVariable long id) {

        return productService.getProductListings(id);
    }
}