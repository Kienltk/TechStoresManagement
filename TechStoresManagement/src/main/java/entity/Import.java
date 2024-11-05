package entity;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Import {
    private final StringProperty importName = new SimpleStringProperty();
    private final StringProperty warehouseName = new SimpleStringProperty();
    private final StringProperty storeName = new SimpleStringProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();

    public Import() {
    }

    // Constructor full
    public Import(String importName, String warehouseName, String storeName, double total, LocalDateTime date, String status) {
        this.importName.set(importName);
        this.warehouseName.set(warehouseName);
        this.storeName.set(storeName);
        this.total.set(total);
        this.date.set(date);
        this.status.set(status);
    }

    // Constructor no StoreManager
    public Import(String importName, String warehouseName, double total, LocalDateTime date, String status) {
        this.importName.set(importName);
        this.warehouseName.set(warehouseName);
        this.total.set(total);
        this.date.set(date);
        this.status.set(status);
    }

    public String getImportName() {
        return importName.get();
    }

    public StringProperty importNameProperty() {
        return importName;
    }

    public void setImportName(String name) {
        this.importName.set(name);
    }

    public String getWarehouseName() {
        return warehouseName.get();
    }

    public void setWarehouseName(String name) {
        this.warehouseName.set(name);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty warehouseNameProperty() {
        return warehouseName;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getStoreName() {
        return storeName.get();
    }

    public void setStoreName(String name) {
        this.storeName.set(name);
    }

    public StringProperty storeNameProperty() {
        return storeName;
    }

    public double getTotal() {
        return total.get();
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    public LocalDateTime getDate() {
        return date.get();
    }

    public void setDate(LocalDateTime date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDateTime> dateProperty() {
        return date;
    }
}