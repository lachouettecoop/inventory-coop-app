package org.coop.inventory.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelStorage {
    public List<InventoryModel> getInventoriesList() {
        return inventoriesList;
    }
    public void addInventories(String id, String date, int state, int rbnId) {
        InventoryModel inventory = new InventoryModel();
        inventory.setId(id);
        inventory.setDate(date);
        inventory.setState(state);
        inventory.setRbnId(rbnId);
        inventoriesList.add(inventory);
    }
    private List<InventoryModel> inventoriesList;

    public static ModelStorage inst() {
        if (instance == null)
            instance = new ModelStorage();
        return instance;
    }
    private ModelStorage() {
        inventoriesList = new ArrayList<>();
        zones = new HashSet<>();
        counters = new HashSet<>();
    }
    private static ModelStorage instance = null;

    public InventoryModel getSelectedInventory() {
        return selectedInventory;
    }
    public void setSelectedInventory(int rbnId) {
        for (InventoryModel inventoryModel: inventoriesList) {
            if (inventoryModel.getRbnId() == rbnId) {
                selectedInventory = inventoryModel;
                return;
            }
        }
    }
    private InventoryModel selectedInventory = null;

    public void clear() {
        for(InventoryModel inventory: inventoriesList) {
            inventory.clear();
        }
        inventoriesList.clear();
        zones.clear();
        counters.clear();
        selectedInventory = null;
        zoneName = null;
        counterName = null;
    }

    public String getZoneName() {
        return zoneName;
    }
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }
    private String zoneName;

    public String getCounterName() {
        return counterName;
    }
    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }
    private String counterName;

    public String[] getZones() {
        return zones.toArray(new String[zones.size()]);
    }
    public void addZone(String zone) {
        this.zones.add(zone);
    }
    private Set<String> zones;

    public String[] getCounters() {
        return counters.toArray(new String[counters.size()]);
    }
    public void addCounter(String counter) {
        this.counters.add(counter);
    }
    private Set<String> counters;
}
