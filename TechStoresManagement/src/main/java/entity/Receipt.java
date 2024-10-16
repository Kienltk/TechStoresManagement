package entity;

import java.time.LocalDateTime;

public class Receipt {
    private int id;
    private String customerName;
    private String storeName;
    private LocalDateTime purchaseDate;
    private double total;
    private double profit;

    public Receipt(int id, String customerName, String storeName, LocalDateTime purchaseDate, double total, double profit) {
        this.id = id;
        this.customerName = customerName;
        this.storeName = storeName;
        this.purchaseDate = purchaseDate;
        this.total = total;
        this.profit = profit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }
}
