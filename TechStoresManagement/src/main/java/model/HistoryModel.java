package model;

import dao.JDBCConnect;
import entity.ProductReceipt;
import entity.Receipt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HistoryModel {
    private final Connection connection;

    public HistoryModel() {
        this.connection = JDBCConnect.getJDBCConnection();
    }

    public ObservableList<Receipt> getReceipts(String customerName) {
        ObservableList<Receipt> receipts = FXCollections.observableArrayList();
        String sql = "SELECT r.id, c.name AS customer_name, s.name AS store_name, r.purchase_date, r.total, r.profit " +
                "FROM receipts r " +
                "JOIN customers c ON r.id_customer = c.id " +
                "JOIN stores s ON r.id_store = s.id";

        // Nếu customerName không phải là null, thêm điều kiện WHERE
        if (customerName != null && !customerName.trim().isEmpty()) {
            sql += " WHERE c.name LIKE ?";
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (customerName != null && !customerName.trim().isEmpty()) {
                statement.setString(1, "%" + customerName + "%");
            }
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Receipt receipt = new Receipt(
                        resultSet.getInt("id"),
                        resultSet.getString("customer_name"),
                        resultSet.getString("store_name"),
                        resultSet.getTimestamp("purchase_date").toLocalDateTime(),
                        resultSet.getDouble("total"),
                        resultSet.getDouble("profit")
                );
                receipts.add(receipt);
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return receipts;
    }


    public ObservableList<ProductReceipt> getProductReceipts(int receiptId) {
        ObservableList<ProductReceipt> productReceipts = FXCollections.observableArrayList();
        String sql = "SELECT pr.id_product, p.product_name, p.brand, p.purchase_price, p.sale_price, pr.quantity, pr.total_amount, pr.profit " +
                "FROM products_receipt pr " +
                "JOIN products p ON pr.id_product = p.id " +
                "WHERE pr.id_receipt = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, receiptId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ProductReceipt productReceipt = new ProductReceipt(
                        resultSet.getInt("id_product"),
                        resultSet.getString("product_name"),
                        resultSet.getString("brand"),
                        resultSet.getDouble("purchase_price"),
                        resultSet.getDouble("sale_price"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("total_amount"),
                        resultSet.getDouble("profit")
                );
                productReceipts.add(productReceipt);
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return productReceipts;
    }

    public String getCustomerPhone(String customerName) {
        String sql = "SELECT phone_number FROM customers WHERE name = ?"; // Giả sử bạn có trường 'name' trong bảng customers
        String phoneNumber = null;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                phoneNumber = resultSet.getString("phone_number");
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return phoneNumber;
    }


    public String getStoreManager(String storeName) {
        String sql = "SELECT CONCAT(e.first_name, ' ', e.last_name) AS manager_name " +
                "FROM employees e " +
                "JOIN stores s ON e.id_store = s.id " + // Giả sử có mối liên hệ giữa bảng employees và stores
                "WHERE s.name = ? AND e.id_role = (SELECT id FROM role WHERE role = 'StoreManager Management')";

        String managerName = null;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, storeName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                managerName = resultSet.getString("manager_name");
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return managerName;
    }


    public String getCashier(int receiptId) {
        String sql = "SELECT CONCAT(e.first_name, ' ', e.last_name) AS cashier_name " +
                "FROM employees e " +
                "JOIN receipts r ON e.id = r.id_cashier " +
                "WHERE r.id = ?";

        String cashierName = null;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, receiptId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                cashierName = resultSet.getString("cashier_name");
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return cashierName;
    }

}
