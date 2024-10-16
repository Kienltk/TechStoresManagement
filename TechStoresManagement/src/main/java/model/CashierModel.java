package model;

import common.ICommon;
import controller.CashierController;
import dao.JDBCConnect;
import entity.Customer;
import entity.Product;
import javafx.scene.control.TableView;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CashierModel implements ICommon<Product> {

    public static void loadData(TableView<Product> productTable, int idStore) {
        ArrayList<Product> products = new CashierModel().getAllFromidStore(idStore);
        productTable.getItems().addAll(products);
    }


    // Helper method to construct Product object from ResultSet
    private Product getProduct(ResultSet rs) throws SQLException {
        String image = rs.getString("img_address");
        int productId = rs.getInt("id");
        String productName = rs.getString("product_name");
        String brand = rs.getString("brand");
        int stock = rs.getInt("quantity");
        double price = rs.getDouble("sale_price");

        // Lấy giá trị của thuộc tính category từ bảng product_categories và categories
        String category = getCategory(productId);

        return new Product(productId, image, productName, brand, stock, price, category);
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

    public Product getProductByName(int idStore, String name) {
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

    public static List<Customer> searchCustomerByPhone(String phoneNumber) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT id, name, phone_number FROM customers WHERE phone_number LIKE ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phoneNumber + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id"); // Lấy ID
                String name = rs.getString("name"); // Lấy tên
                String phone = rs.getString("phone_number"); // Lấy số điện thoại
                customers.add(new Customer(id, name, phone)); // Thêm vào danh sách
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }


    // Thêm khách hàng mới vào database
    public static void addNewCustomer(String name, String phoneNumber) {
        String query = "INSERT INTO customers (name, phone_number) VALUES (?, ?)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, phoneNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Xử lý mua hàng, cập nhật số lượng trong kho
    public static void handlePurchase(int productId, int quantity, int storeId) {
        String query = "UPDATE products_store SET quantity = quantity - ? WHERE id_product = ? AND id_store = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.setInt(3, storeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tạo hóa đơn
    public static int createReceipt(int customerId, int storeId, String employeeName, double total, double profit, Map<Integer, Integer> cartItems) {
        // Câu lệnh SQL sửa đổi để lấy id của nhân viên
        String query = "INSERT INTO receipts (id_customer, id_store, id_cashier, total, profit, purchase_date) " +
                "VALUES (?, ?, (SELECT id FROM employees WHERE CONCAT(first_name, ' ', last_name) = ?), ?, ?, NOW())";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            // Thiết lập các tham số cho PreparedStatement
            stmt.setInt(1, customerId);
            stmt.setInt(2, storeId);
            stmt.setString(3, employeeName); // Đặt employeeName là tham số thứ 3
            stmt.setDouble(4, total);
            stmt.setDouble(5, profit);

            // Thực thi truy vấn
            stmt.executeUpdate();

            // Lấy id của hóa đơn vừa tạo
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int receiptId = rs.getInt(1);
                saveProductsToReceipt(receiptId, cartItems);
                return receiptId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Trả về -1 nếu có lỗi xảy ra
    }


    public static int getCustomerIdByPhone(String phoneNumber) {
        int customerId = -1;
        String query = "SELECT id FROM customers WHERE phone_number = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                customerId = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customerId;  // Nếu không tìm thấy, trả về -1
    }


    public static Product getOne(int productId) {
        Product product = null;
        String query = "SELECT p.id, p.product_name, p.purchase_price, p.sale_price, p.brand, p.img_address, ps.quantity " +
                "FROM products p JOIN products_store ps ON p.id = ps.id_product " +
                "WHERE p.id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                product = new Product(rs.getInt("id"), rs.getString("img_address"),
                        rs.getString("product_name"), rs.getString("brand"),
                        rs.getInt("quantity"), rs.getDouble("sale_price"),
                        rs.getDouble("purchase_price"), "category_placeholder");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return product;
    }

    public static void saveProductsToReceipt(int receiptId, Map<Integer, Integer> cartItems) {
        String query = "INSERT INTO products_receipt (id_receipt, id_product, quantity, total_amount, profit) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                Product product = getOne(productId);

                if (product != null) {
                    double totalAmount = product.getSalePrice() * quantity;
                    double profit = (product.getSalePrice() - product.getPurchasePrice()) * quantity;

                    stmt.setInt(1, receiptId);
                    stmt.setInt(2, productId);
                    stmt.setInt(3, quantity);
                    stmt.setDouble(4, totalAmount);
                    stmt.setDouble(5, profit);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
