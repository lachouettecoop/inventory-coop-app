package org.coop.inventory.model;

public class CountModel {
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    private String id = "";

    public String getCounterName() {
        return counterName;
    }
    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }
    private String counterName = "";

    public String getZoneName() {
        return zoneName;
    }
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }
    private String zoneName = "";

    public double getQty() {
        return qty;
    }
    public void setQty(double qty) {
        this.qty = qty;
    }
    private double qty = 0;

    public String getUpdated() {
        return updated;
    }
    public void setUpdated(String updated) {
        this.updated = updated;
    }
    private String updated = "";
}
