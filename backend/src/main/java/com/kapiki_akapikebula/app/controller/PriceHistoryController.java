package com.kapiki_akapikebula.app.controller;


import com.kapiki_akapikebula.app.dto.PriceHistoryResponse;
import com.kapiki_akapikebula.app.service.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryService priceHistoryService;

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductPriceHistory(
            @PathVariable Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate){
        try {
            List<PriceHistoryResponse> historyResponseList = priceHistoryService.getPriceHistoryById(productId, startDate);
            return ResponseEntity.ok(historyResponseList);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProductPrice(@RequestBody Map<String, Object> requestBody){
        try{
            Long productId = Long.parseLong(requestBody.get("productId").toString());
            BigDecimal newPrice = new BigDecimal(requestBody.get("newPrice").toString());

            priceHistoryService.savePriceHistory(productId, newPrice);
            return ResponseEntity.status(HttpStatus.CREATED).body("Price History updated successfully");
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occured.");
        }
    }
}
