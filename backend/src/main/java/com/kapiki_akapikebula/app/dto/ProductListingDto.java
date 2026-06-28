package com.kapiki_akapikebula.app.dto;

import java.math.BigDecimal;


public record ProductListingDto(
        String shopName,
        BigDecimal price,
        String stockStatus,
        String productUrl
) {}