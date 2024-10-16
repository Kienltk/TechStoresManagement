package entity;

import javafx.beans.property.*;
import javafx.scene.image.ImageView;

public class Product {
    private final SimpleIntegerProperty id;
    private SimpleStringProperty image;
    private final SimpleStringProperty name;
    private final SimpleStringProperty brand;
    private SimpleIntegerProperty stock;
    private final SimpleDoubleProperty salePrice;
    private SimpleDoubleProperty purchasePrice;
    private SimpleStringProperty category;


    // Constructor with all fields
    public Product(int id, String image, String name, String brand, int stock, double salePrice, double purchasePrice, String category) {
        this.id = new SimpleIntegerProperty(id);
        this.image = new SimpleStringProperty(image);
        this.name = new SimpleStringProperty(name);
        this.brand = new SimpleStringProperty(brand);
        this.stock = new SimpleIntegerProperty(stock);
        this.salePrice = new SimpleDoubleProperty(salePrice);
        this.purchasePrice = new SimpleDoubleProperty(purchasePrice);
        this.category = new SimpleStringProperty(category);
    }

    // Constructor without purchasePrice
    public Product(int id, String image, String name, String brand, int stock, double salePrice, String category) {
        this(id, image, name, brand, stock, salePrice, 0.0, category); // Default purchasePrice to 0
    }

    // Constructor with stock = 0 and no purchasePrice
    public Product(int id, String image, String name, String brand, double salePrice, String category) {
        this(id, image, name, brand, 0, salePrice, 0.0, category); // Default stock and purchasePrice to 0
    }

    // Constructor without and category, for minimal product creation
    public Product(int id, String image, String name, String brand, double purchasePrice, double salePrice) {
        this(id, image, name, brand, 0, salePrice, purchasePrice, ""); // Default image and category to empty strings
    }

    public double getSalePrice() {
        return salePrice.get();
    }

    public SimpleDoubleProperty salePriceProperty() {
        return salePrice;
    }

    public double getPurchasePrice() {
        return purchasePrice.get();
    }

    public SimpleDoubleProperty purchasePriceProperty() {
        return purchasePrice;
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public String getImage() {
        return image.get();
    }

    public SimpleStringProperty imageProperty() {
        return image;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getBrand() {
        return brand.get();
    }

    public SimpleStringProperty brandProperty() {
        return brand;
    }

    public int getStock() {
        return stock.get();
    }

    public SimpleIntegerProperty stockProperty() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock.set(stock);
    }

    public String getCategory() { // Thêm phương thức getCategory
        return category.get();
    }

    public SimpleStringProperty categoryProperty() { // Thêm property cho category
        return category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", image=" + image +
                ", name=" + name +
                ", brand=" + brand +
                ", stock=" + stock +
                ", salePrice=" + salePrice +
                ", purchasePrice=" + purchasePrice +
                ", category=" + category +
                '}';
    }
}
