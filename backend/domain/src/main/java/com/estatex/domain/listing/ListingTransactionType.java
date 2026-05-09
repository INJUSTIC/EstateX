package com.estatex.domain.listing;

/**
 * Extensible enum representing what type of transaction a listing is for.
 * Adding a new type requires only a new constant here — no business logic changes elsewhere.
 */
public enum ListingTransactionType {
    RENT,
    SALE,
    EXCHANGE,
    WANTED  // user is looking for accommodation, not offering it
}
