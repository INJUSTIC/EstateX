package com.estatex.adapter.web.controller;

import com.estatex.application.favourite.FavouriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/favourites")
@Tag(name = "Favourites")
public class FavouriteController {

    private final FavouriteService favouriteService;

    public FavouriteController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @GetMapping
    @Operation(summary = "Get saved listings")
    public ResponseEntity<?> getFavourites(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(favouriteService.getFavourites(userId));
    }

    @PostMapping("/{listingId}")
    @Operation(summary = "Save a listing to favourites")
    public ResponseEntity<?> save(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID listingId) {
        return ResponseEntity.ok(favouriteService.saveFavourite(userId, listingId));
    }

    @DeleteMapping("/{listingId}")
    @Operation(summary = "Remove a listing from favourites")
    public ResponseEntity<?> remove(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID listingId) {
        favouriteService.removeFavourite(userId, listingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{listingId}/status")
    @Operation(summary = "Check if a listing is saved")
    public ResponseEntity<?> isFavourite(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID listingId) {
        return ResponseEntity.ok(favouriteService.isFavourite(userId, listingId));
    }
}
