package entity;

import javafx.beans.property.*;
import javafx.scene.image.ImageView;

public class Product {
    private final SimpleIntegerProperty id;
    private final SimpleObjectProperty<ImageView> image;
    private final SimpleStringProperty name;
    private final SimpleStringProperty brand;
    private final SimpleIntegerProperty stock;
    private final SimpleDoubleProperty salePrice;
    private SimpleDoubleProperty purchasePrice;
    private final SimpleStringProperty category; // Thêm thuộc tính category

    public Product(int id, ImageView image, String name, String brand, int stock, double salePrice, String category) {
        this.id = new SimpleIntegerProperty(id);
        this.image = new SimpleObjectProperty<>(image);
        this.name = new SimpleStringProperty(name);
        this.brand = new SimpleStringProperty(brand);
        this.stock = new SimpleIntegerProperty(stock);
        this.salePrice = new SimpleDoubleProperty(salePrice);
        this.category = new SimpleStringProperty(category); // Khởi tạo category
    }

    public Product(int id, ImageView image, String name, String brand, int stock, double salePrice, double purchasePrice, String category) {
        this.id = new SimpleIntegerProperty(id);
        this.image = new SimpleObjectProperty<>(image);
        this.name = new SimpleStringProperty(name);
        this.brand = new SimpleStringProperty(brand);
        this.stock = new SimpleIntegerProperty(stock);
        this.salePrice = new SimpleDoubleProperty(salePrice);
        this.purchasePrice = new SimpleDoubleProperty(purchasePrice);
        this.category = new SimpleStringProperty(category);
    }




    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public ImageView getImage() {
        return image.get();
    }

    public SimpleObjectProperty<ImageView> imageProperty() {
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


    public double getSalePrice() {
        return salePrice.get();
    }

    public double getPurchasePrice() {
        return purchasePrice.get();
    }

    public SimpleDoubleProperty purchasePriceProperty() {
        return purchasePrice;
    }

    public SimpleDoubleProperty salePriceProperty() {
        return salePrice;
    }

    public String getCategory() { // Thêm phương thức getCategory
        return category.get();
    }

    public SimpleStringProperty categoryProperty() { // Thêm property cho category
        return category;
    }
}