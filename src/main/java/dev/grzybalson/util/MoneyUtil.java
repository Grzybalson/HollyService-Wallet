package dev.grzybalson.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {
    public static final long MAX_CENTS = 9_000_000_000_000L;
    private MoneyUtil() {}

    public static long parse(String input) {
        if (input == null || !input.matches("[+-]?[0-9]{1,11}([.,][0-9]{1,2})?")) {
            throw new IllegalArgumentException("Niepoprawna kwota");
        }
        long value = new BigDecimal(input.replace(',', '.')).movePointRight(2).longValueExact();
        if (value < -MAX_CENTS || value > MAX_CENTS) throw new IllegalArgumentException("Kwota przekracza limit");
        return value;
    }

    public static String format(long cents) {
        return BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    public static long add(long balance, long amount) {
        long result = Math.addExact(balance, amount);
        if (result < -MAX_CENTS || result > MAX_CENTS) throw new IllegalArgumentException("Saldo poza zakresem");
        return result;
    }
}
