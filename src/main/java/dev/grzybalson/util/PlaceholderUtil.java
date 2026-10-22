package dev.grzybalson.util;

import java.util.*;

public final class PlaceholderUtil {
    private PlaceholderUtil() {}
    public static Map<String, String> values(String... entries) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) result.put(entries[i], entries[i + 1]);
        return result;
    }
    public static String replace(String text, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) text = text.replace("%" + entry.getKey() + "%", entry.getValue());
        return text;
    }
}
