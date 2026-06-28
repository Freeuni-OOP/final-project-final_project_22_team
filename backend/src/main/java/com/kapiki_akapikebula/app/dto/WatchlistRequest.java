package com.kapiki_akapikebula.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WatchlistRequest {
    private Long productID;
    private BigDecimal targetPrice;
}
