package model;

import dao.JDBCConnect;
import entity.Import;
import entity.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ImportManagementModel {
    public static ObservableList<String> getWarehouseNames() {
        ObservableList<String> warehouseNames = FXCollections.observableArrayList();
        try (Connection conn = JDBCConnect.getJDBCConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM warehouses")) {
            while (rs.next()) {
                warehouseNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warehouseNames;
    }

    private Import getImportForWarehouse(ResultSet rs) throws SQLException {
        String importName = rs.getString("import_name");
        String warehouseName = rs.getString("warehouse_name");
        double totalAmount = rs.getDouble("total");
        LocalDateTime date = rs.getObject("product_import_date", LocalDateTime.class);
        String status = rs.getString("status");
        return new Import(importName, warehouseName, totalAmount, date, status);
    }

    private ArrayList<Import> getImportListForWarehouse(ArrayList<Import> importList, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Import importObj = getImportForWarehouse(rs);
                importList.add(importObj);
            }
        }
        return importList;
    }

    public ArrayList<Import> getAllImportWarehouse(String searchTerm) {
        ArrayList<Import> list = new ArrayList<>();
        String sql = "SELECT import_warehouse.name AS import_name, warehouses.name AS warehouse_name, total, \n" +
                "       product_import_date, status\n" +
                "FROM import_warehouse\n" +
                "JOIN warehouses ON import_warehouse.id_warehouse = warehouses.id\n" +
                "WHERE import_warehouse.name LIKE ?\n" +
                "   OR warehouses.name LIKE ?;";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + searchTerm + "%");
            ps.setString(2, "%" + searchTerm + "%");

            getImportListForWarehouse(list, ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<Import> getAllImportStore(String searchTerm) {
        ArrayList<Import> list = new ArrayList<>();
        String sql = "SELECT import_store.name AS import_name, stores.name AS store_name, warehouses.name AS warehouse_name, " +
                "total, received_date, status " +
                "FROM import_store " +
                "JOIN stores ON import_store.id_store = stores.id " +
                "JOIN warehouses ON import_store.id_warehouse = warehouses.id " +
                "WHERE import_store.name LIKE ? OR stores.name LIKE ? OR warehouses.name LIKE ?;";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + searchTerm + "%");
            ps.setString(2, "%" + searchTerm + "%");
            ps.setString(3, "%" + searchTerm + "%");

            getImportListForStore(list, ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }



    public boolean createInvoiceForWarehouse(Import imp) {
        String sql = "INSERT INTO import_warehouse (id_warehouse, name, total, product_import_date, status)" +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Import getImportForStore(ResultSet rs) throws SQLException {
        String importName = rs.getString("import_name");
        String storeName = rs.getString("store_name");
        String warehouseName = rs.getString("warehouse_name");
        double totalAmount = rs.getDouble("total");
        LocalDateTime date = rs.getObject("received_date", LocalDateTime.class); // sửa tên cột
        String status = rs.getString("status");
        return new Import(importName, warehouseName, storeName, totalAmount, date, status);
    }

    private ArrayList<Import> getImportListForStore(ArrayList<Import> importList, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Import importObj = getImportForStore(rs);
                importList.add(importObj);
            }
        }
        return importList;
    }

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<Product>();
        String sql = "SELECT id,  product_name, brand, purchase_price  FROM products;";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"),
                        rs.getString("product_name"), rs.getString("brand"),
                        (int) rs.getDouble("purchase_price"));
                list.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

