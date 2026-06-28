package com.kapiki_akapikebula.app.service;

import com.kapiki_akapikebula.app.dto.WatchlistRequest;
import com.kapiki_akapikebula.app.dto.WatchlistResponse;
import com.kapiki_akapikebula.app.model.PriceAlert;
import com.kapiki_akapikebula.app.model.Product;
import com.kapiki_akapikebula.app.model.ShopProducts;
import com.kapiki_akapikebula.app.model.User;
import com.kapiki_akapikebula.app.repository.PriceAlertRepository;
import com.kapiki_akapikebula.app.repository.ProductRepository;
import com.kapiki_akapikebula.app.repository.ShopProductsRepository;
import com.kapiki_akapikebula.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopProductsRepository shopProductsRepository;

    public WatchlistResponse addToWatchlist(String email, WatchlistRequest request){
        Optional<User> userOptional = userRepository.findByEmail(email);
        Optional<Product> productOptional = productRepository.findById(request.getProductID());

        if(userOptional.isEmpty()) throw new RuntimeException("Authenticated user not found.");
        if(productOptional.isEmpty()) throw new RuntimeException("Product not found with ID: " + request.getProductID());

        User user = userOptional.get();
        Product product = productOptional.get();

        Optional<PriceAlert> existingAlert = priceAlertRepository.findByUserIdAndProductId(user.getId(), product.getId());

        if(existingAlert.isPresent()) throw new RuntimeException("This product is already in your watchlist.");

        List<ShopProducts> shopPrices = shopProductsRepository.findByProductId(product.getId());

        BigDecimal lowestPrice = shopPrices.stream()
                .map(ShopProducts::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        PriceAlert newPA = new PriceAlert();
        newPA.setUser(user);
        newPA.setProduct(product);
        newPA.setTargetPrice(request.getTargetPrice());
        newPA.setTriggered(false);

        PriceAlert savedAlert = priceAlertRepository.save(newPA);

        return new WatchlistResponse(
                savedAlert.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                lowestPrice,
                savedAlert.getTargetPrice(),
                savedAlert.isTriggered()
        );
    }

    public List<WatchlistResponse> getWatchlist(String email){
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()) throw new RuntimeException("User not found.");

        User user = userOptional.get();

        List<PriceAlert> alerts = priceAlertRepository.findByUserId(user.getId());

        return alerts.stream().map(alert -> {
            Product product = alert.getProduct();
            List<ShopProducts> shopPrices = shopProductsRepository.findByProductId(product.getId());

            BigDecimal lowestPrice = shopPrices.stream()
                    .map(ShopProducts::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            return new WatchlistResponse(
                    alert.getId(),
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    lowestPrice,
                    alert.getTargetPrice(),
                    alert.isTriggered()
            );
        }).toList();
    }
}
