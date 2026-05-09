package com.estatex.domain.listing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void shouldCreateAddressWithAllFieldsWhenFactoryMethodCalled() {
        //when
        Address address = Address.of("ul. Marszałkowska 1", "Warszawa", "Mazowieckie",
                "00-001", "Poland", 52.23, 21.01);

        //then
        assertAll(
                () -> assertEquals("ul. Marszałkowska 1", address.street()),
                () -> assertEquals("Warszawa", address.city()),
                () -> assertEquals("Mazowieckie", address.voivodeship()),
                () -> assertEquals("00-001", address.postalCode()),
                () -> assertEquals("Poland", address.country()),
                () -> assertEquals(52.23, address.latitude()),
                () -> assertEquals(21.01, address.longitude())
        );
    }

    @Test
    void shouldThrowWhenCityIsNull() {
        //when / then
        assertThrows(IllegalArgumentException.class,
                () -> Address.of("Street", null, null, null, "Poland", null, null));
    }

    @Test
    void shouldThrowWhenCityIsBlank() {
        //when / then
        assertThrows(IllegalArgumentException.class,
                () -> Address.of("Street", "   ", null, null, "Poland", null, null));
    }

    @Test
    void shouldDefaultCountryToPolandWhenNullCountryGiven() {
        //when
        Address address = Address.of(null, "Kraków", null, null, null, null, null);

        //then
        assertEquals("Poland", address.country());
    }

    @Test
    void shouldAllowNullOptionalFieldsWhenCreated() {
        //when
        Address address = Address.of(null, "Gdańsk", null, null, "Poland", null, null);

        //then
        assertNull(address.street());
        assertNull(address.voivodeship());
        assertNull(address.postalCode());
        assertNull(address.latitude());
        assertNull(address.longitude());
    }
}
