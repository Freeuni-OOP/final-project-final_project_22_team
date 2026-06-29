package com.kapiki_akapikebula.app.controller;

import com.kapiki_akapikebula.app.dto.WatchlistRequest;
import com.kapiki_akapikebula.app.dto.WatchlistResponse;
import com.kapiki_akapikebula.app.service.JwtUtil;
import com.kapiki_akapikebula.app.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@CrossOrigin(origins = "*")
public class WatchlistController {
    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> addToWatchlist(@RequestHeader("Authorization") String token,
                                            @RequestBody WatchlistRequest request){
        try{
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);

            WatchlistResponse response = watchlistService.addToWatchlist(email, request);
            return ResponseEntity.ok(response);
        } catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch(Exception e){
            return ResponseEntity.internalServerError().body("An unexpected error occurred.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getWatchlist(@RequestHeader("Authorization") String token){
        try{
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);

            List<WatchlistResponse> watchlist = watchlistService.getWatchlist(email);
            return ResponseEntity.ok(watchlist);
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch(Exception e){
            return ResponseEntity.internalServerError().body("An unexpected error occurred.");
        }
    }

}
