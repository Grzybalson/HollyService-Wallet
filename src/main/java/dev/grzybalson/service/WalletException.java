package dev.grzybalson.service;

import java.util.*;

public final class WalletException extends RuntimeException {
    public final String key;
    public final Map<String, String> values;
    public WalletException(String key, Map<String, String> values) {
        super(key);
        this.key = key;
        this.values = values;
    }
    public WalletException(String key) { this(key, Collections.emptyMap()); }
}
