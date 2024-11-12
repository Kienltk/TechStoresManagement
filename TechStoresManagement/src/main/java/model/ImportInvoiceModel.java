package model;

import dao.JDBCConnect;
import entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public ObservableList<ProductInvoice> getProductInvoiceByInvoiceName(String invoiceName) {
        ObservableList<ProductInvoice> productInvoices = FXCollections.observableArrayList();
        String sql = "SELECT p.product_name, p.brand, p.purchase_price, p.sale_price, " +
                "iw.id AS import_id, iwd.id_product, iwd.quantity, iwd.total " +
                "FROM import_warehouse_details iwd " +
                "JOIN products p ON iwd.id_product = p.id " +
                "JOIN import_warehouse iw ON iwd.id_import = iw.id " +
                "WHERE iw.name = ? " +
                "UNION ALL " +
                "SELECT p.product_name, p.brand, p.purchase_price, p.sale_price, " +
                "isd.id_import AS import_id, isd.id_product, isd.quantity, isd.total " +
                "FROM import_store_details isd " +
                "JOIN products p ON isd.id_product = p.id " +
                "JOIN import_store ims ON isd.id_import = ims.id " +
                "WHERE ims.name = ?";// Cả hai bảng đều được truy vấn theo tên hóa đơn

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, invoiceName); // Set tên hóa đơn đầu tiên cho warehouse
            statement.setString(2, invoiceName); // Set tên hóa đơn cho store
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

    public boolean updateProductQuantity(String invoiceName) {
        String sql = "SELECT " +
                "    CASE " +
                "        WHEN iw.name IS NOT NULL THEN 'warehouse' " +
                "        WHEN ims.name IS NOT NULL THEN 'store' " +
                "        ELSE 'unknown' " +
                "    END AS invoice_type " +
                "FROM import_warehouse iw " +
                "LEFT JOIN import_store ims ON iw.name = ims.name " +
                "WHERE iw.name = ? " +
                "UNION " +
                "SELECT " +
                "    CASE " +
                "        WHEN iw.name IS NOT NULL THEN 'warehouse' " +
                "        WHEN ims.name IS NOT NULL THEN 'store' " +
                "        ELSE 'unknown' " +
                "    END AS invoice_type " +
                "FROM import_store ims " +
                "LEFT JOIN import_warehouse iw ON ims.name = iw.name " +
                "WHERE ims.name = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, invoiceName);
            statement.setString(2, invoiceName);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String invoiceType = rs.getString("invoice_type");

                    if ("warehouse".equals(invoiceType)) {
                        return updateWarehouseProductQuantities(invoiceName);
                    } else if ("store".equals(invoiceType)) {
                        return updateStoreProductQuantities(invoiceName);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return false;
    }

    private boolean updateWarehouseProductQuantities(String invoiceName) {
        String selectSql = "SELECT id_product, quantity FROM import_warehouse_details " +
                "WHERE id_import = (SELECT id FROM import_warehouse WHERE name = ?)";

        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, invoiceName);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("id_product");
                    int quantity = rs.getInt("quantity");

                    // Kiểm tra sản phẩm đã tồn tại trong kho hay chưa
                    String checkSql = "SELECT quantity FROM products_warehouse WHERE id_product = ? " +
                            "AND id_warehouse = (SELECT id_warehouse FROM import_warehouse WHERE name = ?)";

                    try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                        checkStatement.setInt(1, productId);
                        checkStatement.setString(2, invoiceName);

                        try (ResultSet checkRs = checkStatement.executeQuery()) {
                            if (checkRs.next()) {
                                // Sản phẩm đã tồn tại, cập nhật số lượng
                                int currentQuantity = checkRs.getInt("quantity");
                                String updateSql = "UPDATE products_warehouse SET quantity = ? WHERE id_product = ? " +
                                        "AND id_warehouse = (SELECT id_warehouse FROM import_warehouse WHERE name = ?)";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                                    updateStatement.setInt(1, currentQuantity + quantity);
                                    updateStatement.setInt(2, productId);
                                    updateStatement.setString(3, invoiceName);
                                    updateStatement.executeUpdate();
                                }
                            } else {
                                // Sản phẩm chưa tồn tại, thực hiện chèn mới
                                String insertSql = "INSERT INTO products_warehouse (id_product, id_warehouse, quantity) " +
                                        "VALUES (?, (SELECT id_warehouse FROM import_warehouse WHERE name = ?), ?)";
                                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                                    insertStatement.setInt(1, productId);
                                    insertStatement.setString(2, invoiceName);
                                    insertStatement.setInt(3, quantity);
                                    insertStatement.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return false;
    }

    private boolean updateStoreProductQuantities(String invoiceName) {
        String selectSql = "SELECT id_product, quantity FROM import_store_details " +
                "WHERE id_import = (SELECT id FROM import_store WHERE name = ?)";

        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, invoiceName);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("id_product");
                    int quantity = rs.getInt("quantity");

                    // Kiểm tra sản phẩm đã tồn tại trong cửa hàng hay chưa
                    String checkSql = "SELECT quantity FROM products_store WHERE id_product = ? " +
                            "AND id_store = (SELECT id_store FROM import_store WHERE name = ?)";

                    try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                        checkStatement.setInt(1, productId);
                        checkStatement.setString(2, invoiceName);

                        try (ResultSet checkRs = checkStatement.executeQuery()) {
                            if (checkRs.next()) {
                                // Sản phẩm đã tồn tại, cập nhật số lượng
                                int currentQuantity = checkRs.getInt("quantity");
                                String updateSql = "UPDATE products_store SET quantity = ? WHERE id_product = ? " +
                                        "AND id_store = (SELECT id_store FROM import_store WHERE name = ?)";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                                    updateStatement.setInt(1, currentQuantity + quantity);
                                    updateStatement.setInt(2, productId);
                                    updateStatement.setString(3, invoiceName);
                                    updateStatement.executeUpdate();
                                }
                            } else {
                                // Sản phẩm chưa tồn tại, thực hiện chèn mới
                                String insertSql = "INSERT INTO products_store (id_product, id_store, quantity) " +
                                        "VALUES (?, (SELECT id_store FROM import_store WHERE name = ?), ?)";
                                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                                    insertStatement.setInt(1, productId);
                                    insertStatement.setString(2, invoiceName);
                                    insertStatement.setInt(3, quantity);
                                    insertStatement.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }

        return false;
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

    public static int createImportInvoiceWarehouse(String nameInvoice, String warehouseName, double total, String requestedDate, Map<Integer, Integer> cartItems) {
        // Câu lệnh SQL sửa đổi để lấy id của nhân viên
        String query = "INSERT INTO import_warehouse (id_warehouse, name, total, created_at, requested_date, actual_import_date, status_id) " +
                "VALUES ((SELECT id FROM warehouses WHERE name = ?), ?, ?, NOW(), ?, null, 1)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, warehouseName);
            stmt.setString(2, nameInvoice);
            stmt.setDouble(3, total);
            stmt.setString(4, requestedDate);

            // Thực thi truy vấn
            stmt.executeUpdate();

            // Lấy id của hóa đơn vừa tạo
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int importInvoiceId = rs.getInt(1);
                saveProductsToImportInvoiceWarehouse(importInvoiceId, cartItems);
                return importInvoiceId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Trả về -1 nếu có lỗi xảy ra
    }

    public static void saveProductsToImportInvoiceWarehouse(int importInvoiceId, Map<Integer, Integer> cartItems) {
        String query = "INSERT INTO import_warehouse_details (id_import, id_product, quantity, total) VALUES (?, ?, ?, ?)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                Product product = getOne(productId);

                if (product != null) {
                    double totalAmount = product.getSalePrice() * quantity;

                    stmt.setInt(1, importInvoiceId);
                    stmt.setInt(2, productId);
                    stmt.setInt(3, quantity);
                    stmt.setDouble(4, totalAmount);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int createImportInvoiceStore(String nameInvoice, String warehouseName, String storeName, double total, String requestedDate, Map<Integer, Integer> cartItems) {
        // Câu lệnh SQL sửa đổi để lấy id của nhân viên
        String query = "INSERT INTO import_store (name, id_store, id_warehouse, total, created_at, requested_date, actual_import_date, status_id) " +
                "VALUES (?, (SELECT id FROM stores WHERE name = ?), (SELECT id FROM warehouses WHERE name = ?), ?, NOW(), ?, null, 1)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nameInvoice);
            stmt.setString(2, storeName);
            stmt.setString(3, warehouseName);
            stmt.setDouble(4, total);
            stmt.setString(5, requestedDate);

            // Thực thi truy vấn
            stmt.executeUpdate();

            // Lấy id của hóa đơn vừa tạo
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int importInvoiceId = rs.getInt(1);
                saveProductsToImportInvoiceStore(importInvoiceId, cartItems);
                return importInvoiceId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Trả về -1 nếu có lỗi xảy ra
    }

    public static void saveProductsToImportInvoiceStore(int importInvoiceId, Map<Integer, Integer> cartItems) {
        String query = "INSERT INTO import_store_details (id_import, id_product, quantity, total) VALUES (?, ?, ?, ?)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                Product product = getOne(productId);

                if (product != null) {
                    double totalAmount = product.getSalePrice() * quantity;

                    stmt.setInt(1, importInvoiceId);
                    stmt.setInt(2, productId);
                    stmt.setInt(3, quantity);
                    stmt.setDouble(4, totalAmount);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getWarehouseNameById(int warehouseId) {
        String sql = "SELECT name FROM warehouses WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, warehouseId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving warehouse name: " + e.getMessage());
        }
        return null; // Trả về null nếu không tìm thấy hoặc xảy ra lỗi
    }

    public String getStoreNameById(int storeId) {
        String sql = "SELECT name FROM stores WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, storeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving store name: " + e.getMessage());
        }
        return null; // Trả về null nếu không tìm thấy hoặc xảy ra lỗi
    }


}
