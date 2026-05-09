package com.estatex.domain.listing;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithGivenAmountWhenConstructed() {
        //given
        BigDecimal amount = new BigDecimal("1500.00");

        //when
        Money money = new Money(amount);

        //then
        assertEquals(amount, money.amount());
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        //given / when / then
        assertThrows(IllegalArgumentException.class,
                () -> new Money(null));
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        //given
        BigDecimal negative = new BigDecimal("-1.00");

        //when / then
        assertThrows(IllegalArgumentException.class,
                () -> new Money(negative));
    }

    @Test
    void shouldAllowZeroAmount() {
        //given
        BigDecimal zero = BigDecimal.ZERO;

        //when
        Money money = new Money(zero);

        //then
        assertEquals(zero, money.amount());
    }

    @Test
    void shouldReturnTrueWhenAmountsAreEqual() {
        //given
        Money a = new Money(new BigDecimal("1000.00"));
        Money b = new Money(new BigDecimal("1000.00"));

        //when
        boolean result = a.isGreaterThanOrEqual(b);

        //then
        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenFirstAmountIsGreater() {
        //given
        Money a = new Money(new BigDecimal("2000.00"));
        Money b = new Money(new BigDecimal("1000.00"));

        //when
        boolean result = a.isGreaterThanOrEqual(b);

        //then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenFirstAmountIsLesser() {
        //given
        Money a = new Money(new BigDecimal("500.00"));
        Money b = new Money(new BigDecimal("1000.00"));

        //when
        boolean result = a.isGreaterThanOrEqual(b);

        //then
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenStrictlyGreater() {
        Money a = new Money(new BigDecimal("2000.00"));
        Money b = new Money(new BigDecimal("1000.00"));
        assertTrue(a.isGreaterThan(b));
    }

    @Test
    void shouldReturnFalseWhenEqualForIsGreaterThan() {
        Money a = new Money(new BigDecimal("1000.00"));
        Money b = new Money(new BigDecimal("1000.00"));
        assertFalse(a.isGreaterThan(b));
    }

    @Test
    void shouldCreateMoneyFromDouble() {
        Money money = Money.pln(15.5);
        assertEquals(new BigDecimal("15.5"), money.amount());
    }

    @Test
    void shouldCreateMoneyFromBigDecimal() {
        Money money = Money.pln(new BigDecimal("20.0"));
        assertEquals(new BigDecimal("20.0"), money.amount());
    }
}
