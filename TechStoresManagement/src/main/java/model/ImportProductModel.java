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

import static model.CashierModel.getOne;

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

    public ArrayList<Import> getAllImportWarehouse() {
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

    public ArrayList<Import> getAllImportStore() {
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

    public Import getOneFromWarehouse(int importId) {
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

    public boolean createInvoiceForWarehouse(Import imp, Map<Integer, Integer> importItems, int idWarehouse) {
        String sql = "INSERT INTO import_warehouse (id_warehouse, name, total, product_import_date, status)" +
                "VALUES (?, ?, ?, ?, ?)";
        String sqlDetails = "INSERT INTO import_warehouse_details (id_import, id_product, quantity, total)" +
                "Values (?, ?, ?, ?);";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             PreparedStatement psDetails = conn.prepareStatement(sqlDetails);) {
            ps.setInt(1, idWarehouse);
            ps.setString(2, imp.getImportName());
            ps.setDouble(3, imp.getTotal());
            ps.setObject(4, imp.getDate());
            ps.setString(5, imp.getStatus());
            for (Map.Entry<Integer, Integer> entry : importItems.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                Product product = getOne(productId);
                if (product != null) {
                    psDetails.setInt(1, getFinalIdFromImportWarehouse());
                    psDetails.setInt(2, product.getId());
                    psDetails.setInt(3, quantity);
                    psDetails.setDouble(4, product.getPurchasePrice() * quantity);
                    psDetails.addBatch();
                }
            }
            ps.executeUpdate();
            psDetails.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createInvoiceForStore(Import imp, Map<Integer, Integer> importItems, int idWarehouse, int idStore) {
        String sql = "INSERT INTO import_store (name, id_store, id_warehouse, total, received_date, status)" +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetails = "INSERT INTO import_store_details (id_import, id_product, quantity, total)" +
                "Values (?, ?, ?, ?);";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             PreparedStatement psDetails = conn.prepareStatement(sqlDetails);) {
            ps.setString(1, imp.getImportName());
            ps.setInt(2, idStore);
            ps.setInt(3, idWarehouse);
            ps.setDouble(4, imp.getTotal());
            ps.setObject(5, imp.getDate());
            ps.setString(6, imp.getStatus());
            for (Map.Entry<Integer, Integer> entry : importItems.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                Product product = getOne(productId);
                if (product != null) {
                    psDetails.setInt(1, getFinalIdFromImportStore());
                    psDetails.setInt(2, product.getId());
                    psDetails.setInt(3, quantity);
                    psDetails.setDouble(4, product.getPurchasePrice() * quantity);
                    psDetails.addBatch();
                }
            }
            ps.executeUpdate();
            psDetails.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Product> getAllImportWarehouseProducts(int importId) {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT id_product" +
                "FROM import_warehouse_details " +
                "JOIN products ON import_warehouse_details.id_product = products.id" +
                "WHERE id_import =?;";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, importId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int productId = rs.getInt("id_product");
                Product product = getOne(productId);
                if (product!= null) {
                    list.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<Product> getAllImportStoreProducts(int importId) {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT id_product" +
                "FROM import_Store_details " +
                "JOIN products ON import_warehouse_details.id_product = products.id" +
                "WHERE id_import =?;";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, importId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int productId = rs.getInt("id_product");
                Product product = getOne(productId);
                if (product!= null) {
                    list.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<Product>();
        String sql = "SELECT id, img_address, product_name, brand, purchase_price  FROM products;";
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


    public int getFinalIdFromImportWarehouse() {
        String sql = "SELECT MAX(id) AS max_id FROM import_warehouse";
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

    public int getFinalIdFromImportStore() {
        String sql = "SELECT MAX(id) AS max_id FROM import_store";
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
