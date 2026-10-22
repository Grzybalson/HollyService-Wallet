package dev.grzybalson.util;

public final class SlotUtil {
    private SlotUtil() {}
    public static int template(int slot, int rows) { return slot >= 27 ? slot - (6 - rows) * 9 : slot; }
    public static void check(int slot, int rows, String path) {
        if (slot < 0 || slot >= rows * 9) throw new IllegalArgumentException(path + ": slot " + slot + " poza GUI");
    }
}
