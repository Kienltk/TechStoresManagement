package model;

import common.ICommon;
import dao.JDBCConnect;
import entity.Product;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class CashierModel implements ICommon<Product> {

    public static void loadSampleData(TableView<Product> productTable, ListView<String> orderListView, Label totalLabel) {
        // Sample product images and data
//        Product product1 = new Product(new ImageView(new Image("file:product1.png")), "Laptop Acer Nitro 5", "Acer", 10, 1000);
//        Product product2 = new Product(new ImageView(new Image("file:product2.png")), "Macbook Pro", "Apple", 5, 1500);

        ArrayList<Product> products = new CashierModel().getAll();

        productTable.getItems().addAll(products);
    }

    @Override
    public ArrayList<Product> getAll() {
        Connection con = JDBCConnect.getJDBCConnection();
        ArrayList<Product> list = new ArrayList<>();
        try {
            String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                    "products_store.quantity, products.sale_price FROM products " +
                    "JOIN products_store ON products.id = products_store.id_product " +
                    "JOIN stores ON products_store.id_store = stores.id " +
                    "WHERE products_store.id_store = 1;";
            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Image image = new Image("file:" + rs.getString("img_address"));
                String name = rs.getString("product_name");
                String brand = rs.getString("brand");
                int stock = rs.getInt("quantity");
                double price = rs.getDouble("sale_price");

                Product product = new Product(new ImageView(image), name, brand, stock, price);
                list.add(product);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Product getOne(long id) {
        return null;
    }

    @Override
    public boolean add(Product obj) {
        return false;
    }

    @Override
    public boolean update(Product obj, int id) {
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }
}
