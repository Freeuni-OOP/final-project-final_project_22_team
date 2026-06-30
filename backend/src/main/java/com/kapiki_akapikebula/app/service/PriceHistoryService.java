package com.kapiki_akapikebula.app.service;

import com.kapiki_akapikebula.app.dto.PriceHistoryResponse;
import com.kapiki_akapikebula.app.model.PriceHistory;
import com.kapiki_akapikebula.app.model.Product;
import com.kapiki_akapikebula.app.repository.PriceHistoryRepository;
import com.kapiki_akapikebula.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PriceHistoryService {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<PriceHistoryResponse> getPriceHistoryById(Long productId, LocalDateTime startDate){
        List<PriceHistory> historyList;

        if(startDate==null){
            historyList = priceHistoryRepository.findByProductIdOrderByRecordedAtAsc(productId);
        }
        else{
            historyList = priceHistoryRepository.getPriceHistory(productId, startDate);
        }

        return historyList.stream().map(p -> new PriceHistoryResponse(p.getPrice(), p.getRecordedAt())).toList();
    }

    public void savePriceHistory(Long productId, BigDecimal newPrice){
        Product product = productRepository.findById(productId).orElseThrow(() -> new NoSuchElementException("Product by id was not found"));

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setProduct(product);
        priceHistory.setPrice(newPrice);

        priceHistoryRepository.save(priceHistory);
    }
}