package model;

import dao.JDBCConnect;
import entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImportInvoiceModel {
    private final Connection connection;
    public ImportInvoiceModel() {
        this.connection = JDBCConnect.getJDBCConnection();
    }

    public ObservableList<ImportInvoice> getAllImportInvoices(String search) {
        ObservableList<ImportInvoice> importInvoices = FXCollections.observableArrayList();
        String sql = "SELECT iw.id AS import_id, iw.name AS import_name, w.name AS warehouse_name, " +
                "w.address AS warehouse_address, NULL AS store_name, NULL AS store_address, " +
                "iw.total AS total_amount, iw.created_at, iw.requested_date, iw.actual_import_date, ims.status_name AS status " +
                "FROM import_warehouse iw " +
                "JOIN warehouses w ON iw.id_warehouse = w.id " +
                "JOIN import_status ims ON iw.status_id = ims.id " +
                "WHERE 1=1 ";

        String sqlStore = "SELECT ims.id AS import_id, ims.name AS import_name, w.name AS warehouse_name, " +
                "w.address AS warehouse_address, s.name AS store_name, s.address AS store_address, " +
                "ims.total AS total_amount, ims.created_at, ims.requested_date, ims.actual_import_date, ist.status_name AS status " +
                "FROM import_store ims " +
                "JOIN warehouses w ON ims.id_warehouse = w.id " +
                "JOIN stores s ON ims.id_store = s.id " +
                "JOIN import_status ist ON ims.status_id = ist.id " +
                "WHERE 1=1 ";

        // Add search condition if search is not null or empty
        if (search != null && !search.trim().isEmpty()) {
            String searchCondition = " AND name LIKE ?";
            sql += searchCondition;
            sqlStore += searchCondition;
        }

        try (PreparedStatement statementWarehouse = connection.prepareStatement(sql);
             PreparedStatement statementStore = connection.prepareStatement(sqlStore)) {

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search + "%";
                statementWarehouse.setString(1, searchPattern);
                statementStore.setString(1, searchPattern);
            }

            // Execute and retrieve results from import_warehouse table
            try (ResultSet rs = statementWarehouse.executeQuery()) {
                while (rs.next()) {
                    ImportInvoice importInvoice = new ImportInvoice(
                            rs.getInt("import_id"),
                            rs.getString("import_name"),
                            rs.getString("warehouse_name"),
                            null,  // Store name is null for warehouse imports
                            rs.getString("warehouse_address"),
                            null,  // Store address is null for warehouse imports
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("requested_date").toLocalDateTime(),
                            rs.getTimestamp("actual_import_date") != null ?
                                    rs.getTimestamp("actual_import_date").toLocalDateTime() : null,
                            rs.getString("status")
                    );
                    importInvoices.add(importInvoice);
                }
            }

            // Execute and retrieve results from import_store table
            try (ResultSet rs = statementStore.executeQuery()) {
                while (rs.next()) {
                    ImportInvoice importInvoice = new ImportInvoice(
                            rs.getInt("import_id"),
                            rs.getString("import_name"),
                            rs.getString("warehouse_name"),
                            rs.getString("store_name"),
                            rs.getString("warehouse_address"),
                            rs.getString("store_address"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("requested_date").toLocalDateTime(),
                            rs.getTimestamp("actual_import_date") != null ?
                                    rs.getTimestamp("actual_import_date").toLocalDateTime() : null,
                            rs.getString("status")
                    );
                    importInvoices.add(importInvoice);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return importInvoices;
    }


    public ObservableList<ImportInvoice> getImportInvoiceWarehouse(int idWarehouse, String search) {
        ObservableList<ImportInvoice> ImportInvoices = FXCollections.observableArrayList();
        String sql = "SELECT iw.id AS import_id, iw.name AS import_name, w.name AS warehouse_name, " +
                "w.address AS warehouse_address, iw.total AS total_amount, ims.status_name AS status, " +
                "iw.created_at, iw.requested_date, iw.actual_import_date " +
                "FROM import_warehouse iw " +
                "JOIN warehouses w ON iw.id_warehouse = w.id " +
                "JOIN import_status ims ON iw.status_id = ims.id " +
                "WHERE iw.id_warehouse = ?";

        // Thêm điều kiện tìm kiếm nếu search không rỗng hoặc null
        if (search != null && !search.trim().isEmpty()) {
            sql += " AND iw.name LIKE ? ";
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idWarehouse);
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search + "%";
                statement.setString(2, searchPattern);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ImportInvoice importInvoice = new ImportInvoice(
                            rs.getInt("import_id"),
                            rs.getString("import_name"),
                            rs.getString("warehouse_name"),
                            rs.getString("warehouse_address"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("requested_date").toLocalDateTime(),
                            rs.getTimestamp("actual_import_date") != null ?
                                    rs.getTimestamp("actual_import_date").toLocalDateTime() : null,
                            rs.getString("status")
                    );
                    ImportInvoices.add(importInvoice);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ImportInvoices;
    }

    public ObservableList<ImportInvoice> getImportInvoiceStore(int idStore, String search) {
        ObservableList<ImportInvoice> ImportInvoices = FXCollections.observableArrayList();
        String sql = "SELECT ims.id AS invoice_id, ims.name AS invoice_name, s.name AS store_name, " +
                "s.address AS store_address, w.name AS warehouse_name, w.address AS warehouse_address, ims.total," +
                " ims.created_at, ims.requested_date, ims.actual_import_date, ist.status_name AS status " +
                "FROM import_store ims " +
                "JOIN stores s ON ims.id_store = s.id " +
                "JOIN warehouses w ON ims.id_warehouse = w.id " +
                "JOIN import_status ist ON ims.status_id = ist.id " +
                "WHERE ims.id_store = ?";

        // Thêm điều kiện tìm kiếm nếu search không rỗng hoặc null
        if (search != null && !search.trim().isEmpty()) {
            sql += " AND ims.name LIKE ? ";
        }

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idStore);
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search + "%";
                statement.setString(2, searchPattern);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ImportInvoice importInvoice = new ImportInvoice(
                            rs.getInt("invoice_id"),
                            rs.getString("invoice_name"),
                            rs.getString("warehouse_name"),
                            rs.getString("store_name"),
                            rs.getString("warehouse_address"),
                            rs.getString("store_address"),
                            rs.getDouble("total"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("requested_date").toLocalDateTime(),
                            rs.getTimestamp("actual_import_date") != null ?
                                    rs.getTimestamp("actual_import_date").toLocalDateTime() : null,
                            rs.getString("status")
                    );
                    ImportInvoices.add(importInvoice);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ImportInvoices;
    }

    public ObservableList<ProductInvoice> getProductInvoice(int idInvoice) {
        ObservableList<ProductInvoice> productInvoices = FXCollections.observableArrayList();
        String sql = "SELECT iwd.id_product, p.product_name, p.brand, p.purchase_price, p.sale_price, iwd.quantity, iwd.total " +
                "FROM import_warehouse_details iwd " +
                "JOIN products p ON iwd.id_product = p.id " +
                "WHERE iwd.id_import = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idInvoice);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ProductInvoice productInvoice = new ProductInvoice(
                        resultSet.getInt("id_product"),
                        resultSet.getString("product_name"),
                        resultSet.getString("brand"),
                        resultSet.getDouble("purchase_price"),
                        resultSet.getDouble("sale_price"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("total")
                );
                productInvoices.add(productInvoice);
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return productInvoices;
    }

    public String getStoreManager(String storeName) {
        String sql = "SELECT CONCAT(e.first_name, ' ', e.last_name) AS manager_name " +
                "FROM employees e " +
                "JOIN stores s ON e.id_store = s.id " +
                "WHERE s.name = ? AND e.id_role = (SELECT id FROM role WHERE role = 'Store Management')";

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

    public String getWarehouseManager(String warehouseName) {
        String sql = "SELECT CONCAT(e.first_name, ' ', e.last_name) AS manager_name " +
                "FROM employees e " +
                "JOIN warehouses w ON e.id_warehouse = w.id " +
                "WHERE w.name = ? AND e.id_role = (SELECT id FROM role WHERE role = 'Warehouse Management')";

        String managerName = null;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, warehouseName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                managerName = resultSet.getString("manager_name");
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return managerName;
    }

    public boolean updateImportStatus(String invoiceName, String status) {
        String sql;

        // Xác định bảng cập nhật dựa trên tên hóa đơn
        sql = "SELECT " +
                "    CASE " +
                "        WHEN iw.name IS NOT NULL THEN 'warehouse'  " +
                "        WHEN ims.name IS NOT NULL THEN 'store'   " +
                "        ELSE 'unknown'  " +
                "    END AS invoice_type " +
                "FROM  " +
                "    import_warehouse iw " +
                "LEFT JOIN  " +
                "    import_store ims ON iw.name = ims.name " +
                "WHERE  " +
                "    iw.name = ? " +
                " " +
                "UNION " +
                " " +
                "SELECT  " +
                "    CASE  " +
                "        WHEN iw.name IS NOT NULL THEN 'warehouse'  " +
                "        WHEN ims.name IS NOT NULL THEN 'store'   " +
                "        ELSE 'unknown'  " +
                "    END AS invoice_type " +
                "FROM  " +
                "    import_store ims " +
                "LEFT JOIN  " +
                "    import_warehouse iw ON ims.name = iw.name " +
                "WHERE  " +
                "    ims.name = ?; ";

        boolean isUpdated = false;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, invoiceName);
            statement.setString(2, invoiceName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String invoiceType = rs.getString("invoice_type");

                    // Cập nhật dữ liệu tùy theo loại hóa đơn (warehouse hoặc store)
                    if ("warehouse".equals(invoiceType)) {
                        sql = "UPDATE import_warehouse SET status_id = (SELECT id FROM import_status WHERE status_name = ?), " +
                                "actual_import_date = CASE WHEN ? = 'Imported' THEN NOW() ELSE actual_import_date END " +
                                "WHERE name = ?";
                    } else if ("store".equals(invoiceType)) {
                        sql = "UPDATE import_store SET status_id = (SELECT id FROM import_status WHERE status_name = ?), " +
                                "actual_import_date = CASE WHEN ? = 'Imported' THEN NOW() ELSE actual_import_date END " +
                                "WHERE name = ?";
                    } else {
                        return false;  // Không tìm thấy loại hóa đơn phù hợp
                    }

                    try (PreparedStatement updateStatement = connection.prepareStatement(sql)) {
                        updateStatement.setString(1, status);
                        updateStatement.setString(2, status);
                        updateStatement.setString(3, invoiceName);
                        int rowsAffected = updateStatement.executeUpdate();
                        isUpdated = rowsAffected > 0;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return isUpdated;
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
    private Product getProduct(ResultSet rs) throws SQLException {
        String image = rs.getString("img_address");
        int productId = rs.getInt("id");
        String productName = rs.getString("product_name");
        String brand = rs.getString("brand");
        double price = rs.getDouble("purchase_price");


        return new Product(productId, image, productName, brand, price);
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
    public ArrayList<Product> getAll() {
        return getAllFromidStore(); // Default to store ID 1 if none provided
    }
    public ArrayList<Product> getAllFromidStore() {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT products.img_address, products.id, products.product_name, products.brand, " +
                "products_store.quantity, products.purchase_price FROM products " +
                "JOIN products_store ON products.id = products_store.id_product " +
                "JOIN stores ON products_store.id_store = stores.id ";

        try (Connection con = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return getProducts(list, ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public static ArrayList<Warehouse> getAllWarehouses() {
        ArrayList<Warehouse> warehouses = new ArrayList<>();
        String query = "SELECT id, name FROM warehouses";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                warehouses.add(new Warehouse(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return warehouses;
    }

    public static ArrayList<Store> getAllStores() {
        ArrayList<Store> stores = new ArrayList<>();
        String query = "SELECT id, name FROM stores";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stores.add(new Store(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stores;
    }

}
