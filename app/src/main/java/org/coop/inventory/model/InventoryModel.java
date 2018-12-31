package org.coop.inventory.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryModel {
    public static int INITIATED = 0;
    public static int ACTIVE = 1;
    public static int CLOSED = 2;

    InventoryModel() {
        productsById = new HashMap<>();
        productsByBarcode = new HashMap<>();
        productsByName = new HashMap<>();
    }

    public List<String> getProductNames() {
        List<String> names = new ArrayList<>();
        for(ProductModel product: productsById.values()) {
            names.add(product.getName());
        }
        return names;
    }

    public List<String> getBarcodes() {
        List<String> names = new ArrayList<>();
        for(ProductModel product: productsById.values()) {
            names.add(product.getBarcode());
        }
        return names;
    }

    public Map<String, ProductModel> getProductsById() {
        return productsById;
    }
    public Map<String, ProductModel> getProductsByBarcode() {
        return productsByBarcode;
    }
    public Map<String, ProductModel> getProductsByName() {
        return productsByName;
    }
    public void addProduct(String id, String name, String barcode) {
        ProductModel product = new ProductModel();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        productsById.put(id, product);
        productsByBarcode.put(barcode, product);
        productsByName.put(name, product);
    }
    private Map<String, ProductModel> productsById;
    private Map<String, ProductModel> productsByBarcode;
    private Map<String, ProductModel> productsByName;

    public void clear() {
        for(ProductModel product: productsById.values()) {
            product.clear();
        }
        productsById.clear();
        productsByBarcode.clear();
        productsByName.clear();
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    private String date = null;

    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    private int state = -1;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    private String id = null;

    public int getRbnId() {
        return rbnId;
    }
    public void setRbnId(int rbnId) {
        this.rbnId = rbnId;
    }
    private int rbnId = 0;
}
