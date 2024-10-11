package model;

import common.ICommon;
import dao.JDBCConnect;
import entity.Product;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableView;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CashierModel implements ICommon<Product> {

    public static void loadData(TableView<Product> productTable, int idStore) {
        ArrayList<Product> products = new CashierModel().getAllFromidStore(idStore);
        productTable.getItems().addAll(products);
    }


    // Helper method to construct Product object from ResultSet
    private Product getProduct(ResultSet rs) throws SQLException {
        Image image = new Image("file:" + rs.getString("img_address"));
        int productId = rs.getInt("id");
        String productName = rs.getString("product_name");
        String brand = rs.getString("brand");
        int stock = rs.getInt("quantity");
        double price = rs.getDouble("sale_price");

        // Lấy giá trị của thuộc tính category từ bảng product_categories và categories
        String category = getCategory(productId);

        return new Product(productId, new ImageView(image), productName, brand, stock, price, category);
    }

    private String getCategory(int productId) {
        String sql = "SELECT c.category_name FROM product_categories pc JOIN categories c ON pc.id_category = c.id WHERE pc.id_product = ?";

        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("category_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Generic method to handle result from prepared statements
    private ArrayList<Product> getProducts(ArrayList<Product> list, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product product = getProduct(rs);
                list.add(product);
            }
        }
        return list;
    }
    public ArrayList<String> getAllCategories() {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT category_name FROM categories";

        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public ArrayList<Product> getAll(int idStore) {
        return getAllFromidStore(idStore); // Default to store ID 1 if none provided
    }

    @Override
    public Product getOne(int idStore, long id) {
        String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                "products_store.quantity, products.sale_price FROM products " +
                "JOIN products_store ON products.id = products_store.id_product " +
                "JOIN stores ON products_store.id_store = stores.id " +
                "WHERE products_store.id_store = ? AND products.id = ?";

        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Set the parameters correctly
            ps.setInt(1, idStore);  // First parameter for id_store
            ps.setLong(2, id);      // Second parameter for products.id

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean add(Product obj) {
        // Implementation for adding a product (if needed)
        return false;
    }

    @Override
    public boolean update(Product obj, int id) {
        // Implementation for updating a product (if needed)
        return false;
    }

    @Override
    public boolean delete(int id) {
        // Implementation for deleting a product (if needed)
        return false;
    }

    public Product getProductByName(int idStore , String name) {
        String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                "products_store.quantity, products.sale_price FROM products " +
                "JOIN products_store ON products.id = products_store.id_product " +
                "JOIN stores ON products_store.id_store = stores.id " +
                "WHERE products_store.id_store = ? AND products.product_name = ?";

        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Product> getAllFromidStore(int idStore) {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                "products_store.quantity, products.sale_price FROM products " +
                "JOIN products_store ON products.id = products_store.id_product " +
                "JOIN stores ON products_store.id_store = stores.id " +
                "WHERE products_store.id_store = ?";

        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idStore);
            return getProducts(list, ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean handlePurchase(int idProduct, int reducedQuantity,int idStore) {
        String sql = "UPDATE products_store SET quantity = quantity - ? WHERE id_product = ? AND id_store = ?";

        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reducedQuantity);
            ps.setInt(2, idProduct);
            ps.setInt(3, idStore);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
