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
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT products.id, product_name, purchase_price, sale_price, brand, img_address " +
                "FROM products " +
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
        String productSql = "INSERT INTO products (product_name, purchase_price, sale_price, brand) " +
                "VALUES (?, ?, ?, ?);";
        String warehouseSql = "INSERT INTO product_warehouse (id_product, id_warehouse, quantity) " +
                "VALUES (?, ?, ?);";

        Connection con = null;
        try {
            // Get database connection
            con = JDBCConnect.getJDBCConnection();
            // Start transaction
            con.setAutoCommit(false);

            // Insert into products table
            try (PreparedStatement psProduct = con.prepareStatement(productSql, Statement.RETURN_GENERATED_KEYS)) {
                productParam(psProduct, obj);
                psProduct.executeUpdate();

                // Retrieve the generated product id (if auto-incremented)
                try (ResultSet rs = psProduct.getGeneratedKeys()) {
                    if (rs.next()) {
                        int productId = rs.getInt(1); // Auto-generated product ID

                        // Insert into product_warehouse table
                        try (PreparedStatement psWarehouse = con.prepareStatement(warehouseSql)) {
                            psWarehouse.setInt(1, productId); // Use the generated product ID
                            psWarehouse.setInt(2, 1);         // id_warehouse = 1
                            psWarehouse.setInt(3, 0);         // quantity = 0
                            psWarehouse.executeUpdate();
                        }
                    }
                }
            }

            // Commit transaction
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Rollback if any error occurs
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Reset to default behavior
                    con.close();             // Close connection
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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
    public boolean delete(int productId) {
        String deleteWarehouseSql = "DELETE FROM product_warehouse WHERE id_product = ?;";
        String deleteProductSql = "DELETE FROM products WHERE id = ?;";
        String resetAutoIncrement = "ALTER TABLE products AUTO_INCREMENT = 1;";

        Connection con = null;
        try {
            // Get database connection
            con = JDBCConnect.getJDBCConnection();
            // Start transaction
            con.setAutoCommit(false);

            // First delete from the product_warehouse table
            try (PreparedStatement psWarehouse = con.prepareStatement(deleteWarehouseSql)) {
                psWarehouse.setInt(1, productId);
                psWarehouse.executeUpdate();
            }

            // Then delete from the products table
            try (PreparedStatement psProduct = con.prepareStatement(deleteProductSql)) {
                psProduct.setInt(1, productId);
                psProduct.executeUpdate();
            }

            // Reset Auto Increment
            try (PreparedStatement psAutoIncrement = con.prepareStatement(resetAutoIncrement)) {
                psAutoIncrement.executeUpdate();
            }

            // Commit transaction
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // Rollback if any error occurs
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Reset to default behavior
                    con.close();             // Close connection
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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
}