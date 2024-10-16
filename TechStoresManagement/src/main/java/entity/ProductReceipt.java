package entity;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProductReceipt {
    private final IntegerProperty idProduct = new SimpleIntegerProperty();
    private final StringProperty productName = new SimpleStringProperty();
    private final StringProperty brand = new SimpleStringProperty();
    private final DoubleProperty purchasePrice = new SimpleDoubleProperty();
    private final DoubleProperty salePrice = new SimpleDoubleProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final DoubleProperty totalAmount = new SimpleDoubleProperty();
    private final DoubleProperty profit = new SimpleDoubleProperty();

    public ProductReceipt(int idProduct, String productName, String brand, double purchasePrice, double salePrice, int quantity, double totalAmount, double profit) {
        this.idProduct.set(idProduct);
        this.productName.set(productName);
        this.brand.set(brand);
        this.purchasePrice.set(purchasePrice);
        this.salePrice.set(salePrice);
        this.quantity.set(quantity);
        this.totalAmount.set(totalAmount);
        this.profit.set(profit);
    }

    public int getIdProduct() {
        return idProduct.get();
    }

    public void setIdProduct(int idProduct) {
        this.idProduct.set(idProduct);
    }

    public String getProductName() {
        return productName.get();
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public String getBrand() {
        return brand.get();
    }

    public void setBrand(String brand) {
        this.brand.set(brand);
    }

    public double getPurchasePrice() {
        return purchasePrice.get();
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice.set(purchasePrice);
    }

    public double getSalePrice() {
        return salePrice.get();
    }

    public void setSalePrice(double salePrice) {
        this.salePrice.set(salePrice);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public double getTotalAmount() {
        return totalAmount.get();
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount.set(totalAmount);
    }

    public double getProfit() {
        return profit.get();
    }

    public void setProfit(double profit) {
        this.profit.set(profit);
    }

    // Observable properties
    public IntegerProperty idProductProperty() {
        return idProduct;
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public StringProperty brandProperty() {
        return brand;
    }

    public DoubleProperty purchasePriceProperty() {
        return purchasePrice;
    }

    public DoubleProperty salePriceProperty() {
        return salePrice;
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public DoubleProperty totalAmountProperty() {
        return totalAmount;
    }

    public DoubleProperty profitProperty() {
        return profit;
    }
}

