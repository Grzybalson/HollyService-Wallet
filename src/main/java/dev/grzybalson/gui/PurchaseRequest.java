package dev.grzybalson.gui;

import dev.grzybalson.config.WalletConfig;
import java.util.*;
import dev.grzybalson.util.MoneyUtil;

public final class PurchaseRequest {
    public final String category;
    public final String productId;
    public final String displayname;
    public final long price;
    public final List<String> commands;
    public PurchaseRequest(String category, String productId, WalletConfig.Product product) {
        this.category = category; this.productId = productId; this.displayname = product.item.displayname;
        this.price = MoneyUtil.parse(product.price);
        this.commands = Collections.unmodifiableList(new ArrayList<>(product.commands));
    }
}
