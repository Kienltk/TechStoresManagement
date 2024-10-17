package model;

import common.ICommon;
import dao.JDBCConnect;
import entity.Product;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.*;
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
        double salePrice = rs.getDouble("sale_price");
        double purchasePrice = rs.getDouble("purchase_price");

        // Lấy giá trị của thuộc tính category từ bảng product_categories và categories
//        String category = getCategory(productId);

//        return new Product(productId, new ImageView(image), productName, brand, stock, price, category);
        return new Product(productId, image, productName, brand, 0, salePrice, purchasePrice, null);
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
        return null;
    }

    public ArrayList<Product> getAll() {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT *" +
                "FROM products ";
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
        String sql = "INSERT INTO products (product_name, purchase_price, sale_price, brand, img_address) " +
                "VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = JDBCConnect.getJDBCConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            productParam(ps, obj);
            ps.setString(5, obj.getImage());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean update(Product obj, int id) {
        return false;
    }

    public boolean update(Product obj) {
        String sql = "UPDATE products SET product_name = ?, purchase_price = ?, sale_price = ?, " +
                "brand = ? WHERE id = ?";
        try (Connection connection = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            productParam(ps, obj);
            ps.setInt(5, obj.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int productId) {
        String deleteProductSql = "DELETE FROM products WHERE id = ?;";
        String resetAutoIncrement = "ALTER TABLE products AUTO_INCREMENT = 1;";

        try (Connection connection = JDBCConnect.getJDBCConnection();
        PreparedStatement ps1 = connection.prepareStatement(deleteProductSql);
        PreparedStatement ps2 = connection.prepareStatement(resetAutoIncrement)) {
            ps1.setInt(1, productId);
            ps1.executeUpdate();
            ps2.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getFinalId() {
        String sql = "SELECT MAX(id) AS max_id FROM products";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                return maxId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean ifDependencies(int id) {
        // TRUE = Still have dependencies
        // FALSE = No dependencies
        String sqlWarehouse = "SELECT COUNT(*) AS count FROM product_warehouse WHERE id_product =?;";
        String sqlStore = "SELECT COUNT(*) AS count FROM products_store WHERE id_product =?";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps1 = con.prepareStatement(sqlWarehouse);
             PreparedStatement ps2 = con.prepareStatement(sqlStore)){
            int count = 0;
            ps1.setInt(1, id);
            ps2.setInt(1, id);
            ResultSet rs1 = ps1.executeQuery();
            ResultSet rs2 = ps2.executeQuery();
            if (rs1.next()) {
                count += rs1.getInt("count");
            }
            if (rs2.next()) {
                count += rs2.getInt("count");
            }
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean ifUniqueProductName (String productName) {
        String sql = "SELECT COUNT(*) AS count FROM products WHERE product_name =?;";
        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
            int count = 0;
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count += rs.getInt("count");
            }
            return count == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}