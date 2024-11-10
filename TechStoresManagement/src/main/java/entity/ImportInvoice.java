package entity;

import java.time.LocalDateTime;

public class ImportInvoice {
    private int id;
    private String invoiceName;
    private String warehouseName;
    private String storeName;
    private String warehouseAddress;
    private String storeAddress;
    private double total;
    private LocalDateTime createdDate;
    private LocalDateTime requestedDate;
    private LocalDateTime actualImportDate;
    private String status;

    public ImportInvoice() {
    }

    public ImportInvoice(int id, String invoiceName, double total, String status) {
        this.id = id;
        this.invoiceName = invoiceName;
        this.total = total;
        this.status = status;
    }

    public ImportInvoice(int id, String invoiceName, String warehouseName, String warehouseAddress, double total, String status) {
        this.id = id;
        this.invoiceName = invoiceName;
        this.warehouseName = warehouseName;
        this.warehouseAddress = warehouseAddress;
        this.total = total;
        this.status = status;
    }

    public ImportInvoice(int id, String invoiceName, String warehouseName, String storeName,
                         String warehouseAddress, String storeAddress, double total,
                         LocalDateTime createdDate, LocalDateTime requestedDate, LocalDateTime actualImportDate, String status) {
        this.id = id;
        this.invoiceName = invoiceName;
        this.warehouseName = warehouseName;
        this.storeName = storeName;
        this.warehouseAddress = warehouseAddress;
        this.storeAddress = storeAddress;
        this.total = total;
        this.createdDate = createdDate;
        this.requestedDate = requestedDate;
        this.actualImportDate = actualImportDate;
        this.status = status;
    }

    public ImportInvoice(int id, String invoiceName, String warehouseName, String warehouseAddress, double total,
                         LocalDateTime createdDate, LocalDateTime requestedDate, LocalDateTime actualImportDate, String status) {
        this.id = id;
        this.invoiceName = invoiceName;
        this.warehouseName = warehouseName;
        this.warehouseAddress = warehouseAddress;
        this.total = total;
        this.createdDate = createdDate;
        this.requestedDate = requestedDate;
        this.actualImportDate = actualImportDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceName() {
        return invoiceName;
    }

    public void setInvoiceName(String invoiceName) {
        this.invoiceName = invoiceName;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getWarehouseAddress() {
        return warehouseAddress;
    }

    public void setWarehouseAddress(String warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalDateTime getActualImportDate() {
        return actualImportDate;
    }

    public void setActualImportDate(LocalDateTime actualImportDate) {
        this.actualImportDate = actualImportDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
