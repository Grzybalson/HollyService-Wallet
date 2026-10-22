package dev.grzybalson.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private TimeUtil() {}
    public static String remaining(long millis) {
        long seconds = Math.max(1, (millis + 999) / 1000);
        return (seconds / 3600) + "h " + (seconds / 60 % 60) + "m " + (seconds % 60) + "s";
    }
    public static String date(long millis, String zone) {
        return DATE.format(Instant.ofEpochMilli(millis).atZone(ZoneId.of(zone)));
    }
}
