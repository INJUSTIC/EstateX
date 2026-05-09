package com.estatex.domain.listing;

/**
 * Immutable value object representing a physical address.
 */
public record Address(
        String street,
        String city,
        String voivodeship,
        String postalCode,
        String country,
        Double latitude,
        Double longitude
) {
    public Address {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City must not be empty");
        }
        if (country == null || country.isBlank()) {
            country = "Poland";
        }
    }

    public static Address of(String street, String city, String voivodeship,
                              String postalCode, String country,
                              Double latitude, Double longitude) {
        return new Address(street, city, voivodeship, postalCode, country, latitude, longitude);
    }
}
