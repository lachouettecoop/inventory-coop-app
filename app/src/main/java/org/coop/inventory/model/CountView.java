package org.coop.inventory.model;

public class CountView {
    public final String productName;
    public final double quantity;
    public final String updateAt;

    public CountView(String productName, double quantity, String updateAt) {
        this.productName = productName;
        this.quantity = quantity;
        this.updateAt = updateAt;
    }
}