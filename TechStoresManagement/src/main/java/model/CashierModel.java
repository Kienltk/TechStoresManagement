package model;

import common.ICommon;
import dao.JDBCConnect;
import entity.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CashierModel implements ICommon<Product> {

    public static void loadData(TableView<Product> productTable, int storeId) {
//        ObservableList<Product> products = FXCollections.observableArrayList();

        ArrayList<Product> products = new CashierModel().getAllFromStoreId(storeId);

        productTable.getItems().addAll(products);
    }

    private Product getProduct(ResultSet rs) throws SQLException {
        Image image = new Image("file:" + rs.getString("img_address"));
        int productId = rs.getInt("id");
        String productName = rs.getString("product_name");
        String brand = rs.getString("brand");
        int stock = rs.getInt("quantity");
        double price = rs.getDouble("sale_price");

        return new Product(productId, new ImageView(image), productName, brand, stock, price);
    }

    private ArrayList<Product> getProducts(ArrayList<Product> list, PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Product product = getProduct(rs);
            list.add(product);
        }
        return list;
    }

    @Override
    public ArrayList<Product> getAll() {
        ArrayList<Product> list = new ArrayList<>();
        try (Connection con = JDBCConnect.getJDBCConnection()) {
            String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                    "products_store.quantity, products.sale_price FROM products " +
                    "JOIN products_store ON products.id = products_store.id_product " +
                    "JOIN stores ON products_store.id_store = stores.id " +
                    "WHERE products_store.id_store = 1;";
            PreparedStatement ps = con.prepareStatement(sql);

            return getProducts(list, ps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Product getOne(long id) {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                "products_store.quantity, products.sale_price FROM products " +
                "JOIN products_store ON products.id = products_store.id_product " +
                "JOIN stores ON products_store.id_store = stores.id " +
                "WHERE products_store.id_store = 1 " +
                "AND products.id = ?";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, (int) id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getProduct(rs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public Product getProductByName(String name) {
        String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                "products_store.quantity, products.sale_price FROM products " +
                "JOIN products_store ON products.id = products_store.id_product " +
                "JOIN stores ON products_store.id_store = stores.id " +
                "WHERE products_store.id_store = 1 " +
                "AND products.product_name = ?";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getProduct(rs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Product> getAllFromStoreId(int storeId) {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                "products_store.quantity, products.sale_price FROM products " +
                "JOIN products_store ON products.id = products_store.id_product " +
                "JOIN stores ON products_store.id_store = stores.id " +
                "WHERE products_store.id_store = ?;";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, 1);

            return getProducts(list, ps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public boolean handlePurchase(int idProduct, int reducedQuantity) {
        try (Connection con = JDBCConnect.getJDBCConnection()) {
            String sql = "UPDATE products_store SET quantity = quantity - ? WHERE id_product = ? AND id_store = 1";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, reducedQuantity);
            ps.setInt(2, idProduct);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
