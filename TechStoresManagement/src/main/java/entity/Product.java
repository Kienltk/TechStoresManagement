package entity;

import javafx.beans.property.*;
import javafx.scene.image.ImageView;

public class Product {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty image;
    private final SimpleStringProperty name;
    private final SimpleStringProperty brand;
    private final SimpleIntegerProperty stock;
    private final SimpleDoubleProperty salePrice;
    private  SimpleDoubleProperty purchasePrice;
    private final SimpleStringProperty category;





    // Thêm thuộc tính category

    public Product(int id, String image, String name, String brand, int stock, double salePrice, double purchasePrice ,String category) {
        this.id = new SimpleIntegerProperty(id);
        this.image = new SimpleStringProperty(image);
        this.name = new SimpleStringProperty(name);
        this.brand = new SimpleStringProperty(brand);
        this.stock = new SimpleIntegerProperty(stock);
        this.salePrice = new SimpleDoubleProperty(salePrice);
        this.purchasePrice = new SimpleDoubleProperty(purchasePrice);
        this.category = new SimpleStringProperty(category); // Khởi tạo category
    }
    public Product(int id, String image, String name, String brand, int stock, double salePrice, String category) {
        this.id = new SimpleIntegerProperty(id);
        this.image = new SimpleStringProperty(image);
        this.name = new SimpleStringProperty(name);
        this.brand = new SimpleStringProperty(brand);
        this.stock = new SimpleIntegerProperty(stock);
        this.salePrice = new SimpleDoubleProperty(salePrice);
        this.category = new SimpleStringProperty(category); // Khởi tạo category
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
}
