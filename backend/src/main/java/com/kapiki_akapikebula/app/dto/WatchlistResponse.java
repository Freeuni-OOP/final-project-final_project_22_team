package com.kapiki_akapikebula.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class WatchlistResponse {
    private Long alertId;
    private Long productId;
    private String productName;
    private String ProductImageUrl;
    private BigDecimal currentPrice;
    private BigDecimal targetPrice;
}
