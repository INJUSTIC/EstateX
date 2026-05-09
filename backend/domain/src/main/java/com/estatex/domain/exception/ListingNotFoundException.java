package com.estatex.domain.exception;

import java.util.UUID;

public class ListingNotFoundException extends DomainException {
    public ListingNotFoundException(UUID id) {
        super("Listing not found: " + id);
    }
}
