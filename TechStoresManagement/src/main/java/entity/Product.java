package entity;

import javafx.beans.property.*;
import javafx.scene.image.ImageView;

public class Product {
    private final SimpleIntegerProperty id;
    private final SimpleObjectProperty<ImageView> image;
    private final SimpleStringProperty name;
    private final SimpleStringProperty brand;
    private final SimpleIntegerProperty stock;
    private final SimpleDoubleProperty price;
    private final SimpleStringProperty category; // Thêm thuộc tính category

    public Product(int id, ImageView image, String name, String brand, int stock, double price, String category) {
        this.id = new SimpleIntegerProperty(id);
        this.image = new SimpleObjectProperty<>(image);
        this.name = new SimpleStringProperty(name);
        this.brand = new SimpleStringProperty(brand);
        this.stock = new SimpleIntegerProperty(stock);
        this.price = new SimpleDoubleProperty(price);
        this.category = new SimpleStringProperty(category); // Khởi tạo category
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


    public double getPrice() {
        return price.get();
    }

    public SimpleDoubleProperty priceProperty() {
        return price;
    }

    public String getCategory() { // Thêm phương thức getCategory
        return category.get();
    }

    public SimpleStringProperty categoryProperty() { // Thêm property cho category
        return category;
    }
}
