package com.kapiki_akapikebula.app.scheduler;
import com.kapiki_akapikebula.app.model.PriceHistory;
import com.kapiki_akapikebula.app.model.ShopProducts;
import com.kapiki_akapikebula.app.repository.PriceHistoryRepository;
import com.kapiki_akapikebula.app.repository.ShopProductsRepository;
import com.kapiki_akapikebula.app.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;




@Component
@RequiredArgsConstructor
public class ScheduledScraperRunner {
    private final ShopProductsRepository shopProductsRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ScraperService scraperService;
    private static final Logger log =  LoggerFactory.getLogger(ScheduledScraperRunner.class);

    @Scheduled(fixedDelay = 43200000)
    @Transactional
    public void runScrapingJobs(){
        log.info("scheduled scrapping has started...");

        List<ShopProducts> allProdListing = shopProductsRepository.findAll();

        for(ShopProducts prod : allProdListing){
            try{
                BigDecimal newPrice = scraperService.scrapeLatestPrice(prod.getProductUrl(), prod.getPrice());

                if(newPrice != null && prod.getPrice().compareTo(newPrice) != 0){
                    log.info("Price changed for " + prod.getProductUrl() + " from " + prod.getPrice() + " to "
                            + newPrice
                    );
                    PriceHistory hist = new PriceHistory();
                    hist.setProduct(prod.getProduct());
                    hist.setPrice(newPrice);
                    priceHistoryRepository.save(hist);
                    prod.setPrice(newPrice);
                    prod.setLastUpdated(LocalDateTime.now());
                    shopProductsRepository.save(prod);
            }

            }catch(Exception e){
                log.error("we caught an error while scraping " + prod.getProductUrl() +
                        " error: " + e.getMessage());
            }
        }
        log.info("finished scheduled scrapping.");
    }
}
