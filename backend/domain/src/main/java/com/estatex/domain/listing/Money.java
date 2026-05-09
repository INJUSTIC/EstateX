package com.estatex.domain.listing;

import java.math.BigDecimal;

/**
 * Immutable money value object.
 */
public record Money(BigDecimal amount) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
    }

    public static Money pln(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money pln(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }
}
