package model;

import common.ICommon;
import dao.JDBCConnect;
import entity.Import;
import entity.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class ImportProductModel {

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

    private Import getImportForStore(ResultSet rs) throws SQLException {
        String importName = rs.getString("import_name");
        String storeName = rs.getString("store_name");
        String warehouseName = rs.getString("warehouse_name");
        double totalAmount = rs.getDouble("total");
        LocalDateTime date = rs.getObject("product_import_date", LocalDateTime.class);
        String status = rs.getString("status");
        return new Import(importName, warehouseName, storeName, totalAmount, date, status);
    }

    private ArrayList<Import> getImportListForStore(ArrayList<Import> importList, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Import importObj = getImportForStore(rs);
                importList.add(importObj);
            }
            return importList;
        }
    }

    private ArrayList<Import> getAllImportWarehouse() {
        ArrayList<Import> list = new ArrayList<Import>();
        String sql = "SELECT import_warehouse.name as import_name, warehouses.name, total, " +
                "product_import_date, status " +
                "FROM import_warehouse" +
                "JOIN warehouses ON import_warehouse.id_warehouse = warehouses.id;";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            getImportListForWarehouse(list, ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ArrayList<Import> getAllImportStore() {
        ArrayList<Import> list = new ArrayList<Import>();
        String sql = "SELECT import_store.name as import_name, stores.name, warehouses.name, total, " +
                "received_date, status " +
                "FROM import_store" +
                "JOIN stores ON import_store.id_store = stores.id" +
                "JOIN warehouses ON import_store.id_warehouse = warehouses.warehouse.id;";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            getImportListForStore(list, ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Import getOneFromWarehouse(int importId) {
        Import imp = new Import();
        String sql = "SELECT import_warehouse.name as import_name, warehouses.name, total, " +
                "product_import_date, status " +
                "FROM import_warehouse" +
                "JOIN warehouses ON import_warehouse.id_warehouse = warehouses.id" +
                "where import_warehouse.id = ?;";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, importId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    imp = getImportForWarehouse(rs);
                }
            }
            return imp;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean createInvoiceForWarehouse(Import imp, Map<Integer, Integer> importItems) {
        String sql = "INSERT INTO import_warehouse (id_warehouse, name, total, product_import_date, status)" +
                "VALUES (?, ?, ?, ?, ?)";
        String sqlDetails = "INSERT INTO import_warehouse_details (id_import, id_product, quantity, total)" +
                "Values (?, ?, ?, ?);";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             PreparedStatement psDetails = conn.prepareStatement(sqlDetails);) {
            ps.setInt(1, importItems.size());
            ps.setString(2, imp.getImportName());
            ps.setDouble(3, imp.getTotal());
            ps.setObject(4, imp.getDate());
            ps.setString(5, imp.getStatus());
            for (Map.Entry<Integer, Integer> entry : importItems.entrySet()) {

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<Product>();
        String sql = "SELECT id, img_address, product_name, brand, purchase_price category FROM products;";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"), rs.getString("img_address"),
                        rs.getString("product_name"), rs.getString("brand"),
                        rs.getDouble("purchase_price"));
                list.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
