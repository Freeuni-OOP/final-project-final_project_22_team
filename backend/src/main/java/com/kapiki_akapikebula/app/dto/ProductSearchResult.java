package com.kapiki_akapikebula.app.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductSearchResult(
        Long productId,
        String name,
        String brand,
        String imageUrl,
        BigDecimal lowestPrice,
        List<ProductListingDto> listings
) {}