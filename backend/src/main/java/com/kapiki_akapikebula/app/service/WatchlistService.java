package com.kapiki_akapikebula.app.service;

import com.kapiki_akapikebula.app.dto.WatchlistRequest;
import com.kapiki_akapikebula.app.dto.WatchlistResponse;
import com.kapiki_akapikebula.app.repository.PriceAlertRepository;
import com.kapiki_akapikebula.app.repository.ProductRepository;
import com.kapiki_akapikebula.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WatchlistService {

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public WatchlistResponse addToWatchlist(String email, WatchlistRequest request){

    }


}
