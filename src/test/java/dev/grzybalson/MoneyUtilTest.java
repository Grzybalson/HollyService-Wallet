package dev.grzybalson;

import dev.grzybalson.util.MoneyUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoneyUtilTest {
    @Test void amountsUseExactCents() {
        assertEquals(10, MoneyUtil.parse("0.1"));
        assertEquals(50, MoneyUtil.parse("0,50"));
        assertEquals(0, MoneyUtil.parse("0"));
        assertEquals(-100, MoneyUtil.parse("-1"));
        assertEquals(1, MoneyUtil.parse("0.01"));
        assertEquals(-1, MoneyUtil.parse("-0,01"));
        assertEquals("123.45", MoneyUtil.format(12345));
        assertEquals("-0.10", MoneyUtil.format(-10));
        assertEquals(30, MoneyUtil.add(MoneyUtil.parse("0.10"), MoneyUtil.parse("0.20")));
    }
    @Test void rejectsAmbiguousAndInvalidValues() {
        for (String amount : new String[]{"NaN", "Infinity", "1e3", "0.001", "1,234.56", "", " 1", "90000000001", "-90000000001"}) {
            assertThrows(IllegalArgumentException.class, () -> MoneyUtil.parse(amount), amount);
        }
        assertThrows(IllegalArgumentException.class, () -> MoneyUtil.add(MoneyUtil.MAX_CENTS, 1));
        assertEquals(-1, MoneyUtil.add(0, -1));
        assertThrows(IllegalArgumentException.class, () -> MoneyUtil.add(-MoneyUtil.MAX_CENTS, -1));
    }
}
