package com.kapiki_akapikebula.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PriceHistoryResponse {
    private BigDecimal price;

    private LocalDateTime recordedAt;
}
