package model;

import common.ICommon;
import dao.JDBCConnect;
import entity.Product;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DirectorModel implements ICommon<Product> {

    private void productParam(PreparedStatement ps, Product product) throws SQLException {
        ps.setString(1, product.getName());
        ps.setDouble(2, product.getPurchasePrice());
        ps.setDouble(3, product.getSalePrice());
        ps.setString(4, product.getBrand());
    }

    private Product getProduct(ResultSet rs) throws SQLException {
        String image = rs.getString("img_address");
        int productId = rs.getInt("id");
        String productName = rs.getString("product_name");
        String brand = rs.getString("brand");
        int stock = rs.getInt("stock");
        double salePrice = rs.getDouble("sale_price");
        double purchasePrice = rs.getDouble("purchase_price");

        // Lấy giá trị của thuộc tính category từ bảng product_categories và categories
//        String category = getCategory(productId);

//        return new Product(productId, new ImageView(image), productName, brand, stock, price, category);
        return new Product(productId,image, productName, brand, stock, salePrice, purchasePrice, null);
    }

    private ArrayList<Product> getProducts(ArrayList<Product> list, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product product = getProduct(rs);
                list.add(product);
            }
        }
        return list;
    }

    @Override
    public ArrayList<Product> getAll(int idStore) {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT products.id, product_name, purchase_price, sale_price, brand, img_address, " +
                "SUM(quantity) AS stock FROM products " +
                "JOIN product_warehouse ON products.id = product_warehouse.id_product " +
                "GROUP BY products.id;";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return getProducts(list, ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Product getOne(int idStore, long id) {
        return null;
    }

    @Override
    public boolean add(Product obj) {
        String sql = "INSERT INTO products (product_name, purchase_price, sale_price, brand)" +
                "VALUES (?, ?, ?, ?)";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            productParam(ps, obj);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Product obj, int id) {
        String sql = "UPDATE products SET product_name = ?, purchase_price = ?, sale_price = ?" +
                "brand = ?";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            productParam(ps, obj);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id =?";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}