package com.kapiki_akapikebula.app.service;

import com.kapiki_akapikebula.app.dto.ProductListingDto;
import com.kapiki_akapikebula.app.model.ShopProducts;
import com.kapiki_akapikebula.app.repository.ShopProductsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ShopProductsRepository shopProductsRep;

    public ProductService(ShopProductsRepository shopProductsRepository) {
        this.shopProductsRep = shopProductsRepository;
    }


    public List<ProductListingDto> getProductListings(long productId) {
        List<ShopProducts> listings = shopProductsRep.findByProductIdOrderByPriceAsc(productId);

        if (listings.isEmpty()) {
            throw new RuntimeException("Product listings not found for ID: " + productId);
        }
        
        return listings.stream()
                .map(listing -> new ProductListingDto(
                        listing.getShop() != null ? listing.getShop().getName() : "Unknown Shop",
                        listing.getPrice(),
                        listing.getStockStatus(),
                        listing.getProductUrl()
                ))
                .toList();
    }
}
