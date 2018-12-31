package org.coop.inventory.model;

import java.util.HashMap;
import java.util.Map;

public class ProductModel {
    ProductModel() {
        counts = new HashMap<>();
    }

    public Map<String, CountModel> getCounts() {
        return counts;
    }
    public void addCount(String id, String zoneName, String counterName, double qty, String updated) {
        CountModel count = new CountModel();
        count.setId(id);
        count.setZoneName(zoneName);
        count.setCounterName(counterName);
        count.setQty(qty);
        count.setUpdated(updated);
        counts.put(id, count);
    }
    private Map<String, CountModel> counts;

    public void clear() {
        counts.clear();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    private String id = null;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    private String name = null;

    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    private String barcode = null;
}
