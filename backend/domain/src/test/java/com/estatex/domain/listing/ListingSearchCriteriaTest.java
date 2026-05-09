package com.estatex.domain.listing;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListingSearchCriteriaTest {

    @Test
    void shouldCreateEmptyCriteria() {
        ListingSearchCriteria criteria = ListingSearchCriteria.empty();
        assertNotNull(criteria);
    }
}
