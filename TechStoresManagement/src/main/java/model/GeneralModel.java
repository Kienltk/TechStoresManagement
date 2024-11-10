package model;

import dao.JDBCConnect;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class GeneralModel {

    private double turnover;
    private double capital;
    private double profit;
    private int stock;

    public void insertStoreFinancialData() throws SQLException {
        String storeTurnoverQuery = "SELECT id_store, SUM(total) AS turnover, SUM(profit) AS profit FROM receipts WHERE DATE(purchase_date) = CURDATE() GROUP BY id_store";
        String storeCapitalQuery = "SELECT id_store, SUM(total) AS capital FROM import_store WHERE DATE(actual_import_date) = CURDATE() GROUP BY id_store";
        String lastStoreFinancialQuery = "SELECT id_store, turnover, capital, profit FROM store_financial WHERE date = CURDATE() ORDER BY id DESC";
        String insertStoreFinancialQuery = "INSERT INTO store_financial (id_store, date, turnover, capital, profit) VALUES (?, CURDATE(), ?, ?, ?)";

        // Sử dụng map để lưu trữ capital cho từng store
        Map<Integer, Double> capitalMap = new HashMap<>();

        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            // Tính toán turnover và profit
            PreparedStatement turnoverStmt = conn.prepareStatement(storeTurnoverQuery);
            ResultSet turnoverResult = turnoverStmt.executeQuery();

            // Tính toán capital từ import_store
            PreparedStatement capitalStmt = conn.prepareStatement(storeCapitalQuery);
            ResultSet capitalResult = capitalStmt.executeQuery();
            while (capitalResult.next()) {
                int idStore = capitalResult.getInt("id_store");
                double capital = capitalResult.getDouble("capital");
                capitalMap.put(idStore, capital);
            }

            // Lưu trữ turnover, profit và capital gần nhất
            Map<Integer, Double> lastTurnoverMap = new HashMap<>();
            Map<Integer, Double> lastProfitMap = new HashMap<>();
            Map<Integer, Double> lastCapitalMap = new HashMap<>();

            // Lấy giá trị gần nhất
            PreparedStatement lastStoreFinancialStmt = conn.prepareStatement(lastStoreFinancialQuery);
            ResultSet lastStoreFinancialResult = lastStoreFinancialStmt.executeQuery();
            while (lastStoreFinancialResult.next()) {
                int idStore = lastStoreFinancialResult.getInt("id_store");
                lastTurnoverMap.put(idStore, lastStoreFinancialResult.getDouble("turnover"));
                lastCapitalMap.put(idStore, lastStoreFinancialResult.getDouble("capital"));
                lastProfitMap.put(idStore, lastStoreFinancialResult.getDouble("profit"));
            }

            // Chèn vào store_financial
            while (turnoverResult.next()) {
                int idStore = turnoverResult.getInt("id_store");
                double turnover = turnoverResult.getDouble("turnover");
                double profit = turnoverResult.getDouble("profit");

                double capital = capitalMap.getOrDefault(idStore, 0.0); // Mặc định capital là 0 nếu không tìm thấy trong import_store

                // Kiểm tra giá trị có giống nhau không
                System.out.println(turnover + " " + lastTurnoverMap.getOrDefault(idStore, 0.0));
                if (lastTurnoverMap.getOrDefault(idStore, 0.0) == turnover &&
                        lastCapitalMap.getOrDefault(idStore, 0.0) == capital &&
                        lastProfitMap.getOrDefault(idStore, 0.0) == profit) {
                    return; // Không thực hiện insert nếu giống
                }

                // Chèn vào store_financial
                try (PreparedStatement insertStmt = conn.prepareStatement(insertStoreFinancialQuery)) {
                    insertStmt.setInt(1, idStore);
                    insertStmt.setDouble(2, turnover);
                    insertStmt.setDouble(3, capital);
                    insertStmt.setDouble(4, profit);
                    insertStmt.executeUpdate();
                }
            }
        }
    }


    public void insertBusinessFinancialData() throws SQLException {
        String lastBusinessFinancialQuery = "SELECT turnover, capital, profit FROM business_financial WHERE date = CURDATE() ORDER BY id DESC LIMIT 1";
        String businessTurnoverQuery = "SELECT turnover FROM store_financial WHERE date = CURDATE() ORDER BY id DESC LIMIT 1";
        String businessProfitQuery = "SELECT profit FROM store_financial WHERE date = CURDATE() ORDER BY id DESC LIMIT 1";
        String warehouseCapitalQuery = "SELECT total FROM import_warehouse WHERE DATE(actual_import_date) = CURDATE() ORDER BY id DESC LIMIT 1";
        String insertBusinessFinancialQuery = "INSERT INTO business_financial (date, turnover, capital, profit) VALUES (CURDATE(), ?, ?, ?)";

        double totalTurnover = 0;
        double totalCapital = 0;
        double totalProfit = 0;

        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            // Lấy turnover từ store_financial
            PreparedStatement turnoverStmt = conn.prepareStatement(businessTurnoverQuery);
            ResultSet turnoverResult = turnoverStmt.executeQuery();
            if (turnoverResult.next()) {
                totalTurnover = turnoverResult.getDouble("turnover");
            }

            // Lấy profit từ store_financial
            PreparedStatement profitStmt = conn.prepareStatement(businessProfitQuery);
            ResultSet profitResult = profitStmt.executeQuery();
            if (profitResult.next()) {
                totalProfit = profitResult.getDouble("profit");
            }

            // Lấy capital từ import_warehouse
            PreparedStatement warehouseCapitalStmt = conn.prepareStatement(warehouseCapitalQuery);
            ResultSet warehouseCapitalResult = warehouseCapitalStmt.executeQuery();
            if (warehouseCapitalResult.next()) {
                double newWarehouseCapital = warehouseCapitalResult.getDouble("total");
                // Kiểm tra capital gần nhất trong ngày
                if (newWarehouseCapital == getLastWarehouseCapital(conn)) {
                    newWarehouseCapital = 0; // Nếu giống nhau, coi capital là 0
                }
                totalCapital += newWarehouseCapital;
            }

            // Lấy thông tin tài chính gần nhất
            PreparedStatement lastFinancialStmt = conn.prepareStatement(lastBusinessFinancialQuery);
            ResultSet lastFinancialResult = lastFinancialStmt.executeQuery();
            double lastTurnover = 0, lastCapital = 0, lastProfit = 0;
            if (lastFinancialResult.next()) {
                lastTurnover = lastFinancialResult.getDouble("turnover");
                lastCapital = lastFinancialResult.getDouble("capital");
                lastProfit = lastFinancialResult.getDouble("profit");
            }

            // Kiểm tra nếu giá trị mới giống với giá trị gần nhất thì không insert
            if (totalTurnover == lastTurnover && totalCapital == lastCapital && totalProfit == lastProfit) {
                return; // Không insert
            }

            // Chèn vào bảng business_financial
            try (PreparedStatement insertStmt = conn.prepareStatement(insertBusinessFinancialQuery)) {
                insertStmt.setDouble(1, totalTurnover);
                insertStmt.setDouble(2, totalCapital);
                insertStmt.setDouble(3, totalProfit);
                insertStmt.executeUpdate();
            }
        }
    }

    // Phương thức để lấy capital gần nhất từ import_warehouse
    private double getLastWarehouseCapital(Connection conn) throws SQLException {
        String query = "SELECT SUM(total) AS capital FROM import_warehouse WHERE DATE(actual_import_date) = CURDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("capital");
            }
        }
        return 0;
    }


    public int calculateTotalStock() throws SQLException {
        String storeStockQuery = "SELECT SUM(quantity) AS total_stock FROM products_store";
        String warehouseStockQuery = "SELECT SUM(quantity) AS total_stock FROM products_warehouse";

        int totalStock = 0;

        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            // Tính tổng hàng tồn kho từ cửa hàng
            PreparedStatement storeStockStmt = conn.prepareStatement(storeStockQuery);
            ResultSet storeStockResult = storeStockStmt.executeQuery();
            if (storeStockResult.next()) {
                totalStock += storeStockResult.getInt("total_stock");
            }

            // Tính tổng hàng tồn kho từ kho
            PreparedStatement warehouseStockStmt = conn.prepareStatement(warehouseStockQuery);
            ResultSet warehouseStockResult = warehouseStockStmt.executeQuery();
            if (warehouseStockResult.next()) {
                totalStock += warehouseStockResult.getInt("total_stock");
            }
        }

        return totalStock; // Trả về tổng số lượng hàng tồn kho
    }


    public double getTurnover() throws SQLException {
        String turnoverQuery = "SELECT SUM(turnover) AS total FROM business_financial";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(turnoverQuery);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    public double getTurnoverStore(int idStore) throws SQLException {
        String turnoverQuery = "SELECT SUM(turnover) AS total FROM store_financial Where id_store = ?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(turnoverQuery)) {
            stmt.setInt(1, idStore);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0;
    }

    public double getCapital() throws SQLException {
        String capitalQuery = "SELECT SUM(capital) AS total FROM business_financial";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(capitalQuery);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    public double getCapitalStore(int idStore) throws SQLException {
        String capitalQuery = "SELECT SUM(capital) AS total FROM store_financial Where id_store = ?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(capitalQuery)) {
            stmt.setInt(1, idStore);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0;
    }

    public double getProfit() throws SQLException {
        String profitQuery = "SELECT SUM(profit) AS total FROM business_financial";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(profitQuery);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }

    public double getProfitStore(int idStore) throws SQLException {
        String profitQuery = "SELECT SUM(profit) AS total FROM store_financial WHERE id_store =?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(profitQuery)) {
            stmt.setInt(1, idStore);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0;
    }

    public int getStock() throws SQLException {
        return calculateTotalStock();
    }

    public int getStockWarehouse(int idWarehouse) {
        String warehouseStockQuery = "SELECT SUM(quantity) AS total_stock FROM products_warehouse WHERE id_warehouse = ?";
        int totalStock = 0;

        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            PreparedStatement warehouseStockStmt = conn.prepareStatement(warehouseStockQuery);
            warehouseStockStmt.setInt(1, idWarehouse);
            ResultSet warehouseStockResult = warehouseStockStmt.executeQuery();
            if (warehouseStockResult.next()) {
                totalStock += warehouseStockResult.getInt("total_stock");
            }

        } catch (SQLException e) {
            System.out.println("Error getting stock from warehouse: " + e.getMessage());
        }
        return totalStock;
    }

    public int getStockStore(int idStore) {
        String warehouseStockQuery = "SELECT SUM(quantity) AS total_stock FROM products_store WHERE id_store = ?";
        int totalStock = 0;

        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            PreparedStatement warehouseStockStmt = conn.prepareStatement(warehouseStockQuery);
            warehouseStockStmt.setInt(1, idStore);
            ResultSet warehouseStockResult = warehouseStockStmt.executeQuery();
            if (warehouseStockResult.next()) {
                totalStock += warehouseStockResult.getInt("total_stock");
            }

        } catch (SQLException e) {
            System.out.println("Error getting stock from warehouse: " + e.getMessage());
        }
        return totalStock;
    }

    public Map<Integer, Map<String, BigDecimal>> getFinancialData() {
        Map<Integer, Map<String, BigDecimal>> financialData = new HashMap<>();
        String query = "SELECT YEAR(date) AS year, SUM(turnover) AS totalTurnover, " +
                "SUM(capital) AS totalCapital, SUM(profit) AS totalProfit " +
                "FROM business_financial GROUP BY YEAR(date)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int year = rs.getInt("year");
                BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                BigDecimal totalCapital = rs.getBigDecimal("totalCapital");
                BigDecimal totalProfit = rs.getBigDecimal("totalProfit");

                // Create a map for the year data
                Map<String, BigDecimal> yearData = new HashMap<>();
                yearData.put("turnover", totalTurnover);
                yearData.put("capital", totalCapital);
                yearData.put("profit", totalProfit);

                // Put the year and the corresponding financial data into the map
                financialData.put(year, yearData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return financialData;
    }

    public Map<Integer, Map<String, BigDecimal>> getStoreFinancialData(int idStore) {
        Map<Integer, Map<String, BigDecimal>> financialData = new HashMap<>();
        String query = "SELECT YEAR(date) AS year, SUM(turnover) AS totalTurnover, " +
                "SUM(capital) AS totalCapital, SUM(profit) AS totalProfit " +
                "FROM store_financial WHERE id_store = ? GROUP BY YEAR(date)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idStore);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int year = rs.getInt("year");
                    BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                    BigDecimal totalCapital = rs.getBigDecimal("totalCapital");
                    BigDecimal totalProfit = rs.getBigDecimal("totalProfit");

                    // Create a map for the year data
                    Map<String, BigDecimal> yearData = new HashMap<>();
                    yearData.put("turnover", totalTurnover);
                    yearData.put("capital", totalCapital);
                    yearData.put("profit", totalProfit);

                    // Put the year and the corresponding financial data into the map
                    financialData.put(year, yearData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return financialData;
    }

    public Map<Integer, Map<String, BigDecimal>> getFinancialDataByYear(int year) {
        Map<Integer, Map<String, BigDecimal>> financialData = new HashMap<>();
        String query = "SELECT MONTH(date) AS month, " +
                "SUM(turnover) AS totalTurnover, " +
                "SUM(capital) AS totalCapital, " +
                "SUM(profit) AS totalProfit " +
                "FROM business_financial " +
                "WHERE YEAR(date) = ? " +
                "GROUP BY MONTH(date)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("month");
                    BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                    BigDecimal totalCapital = rs.getBigDecimal("totalCapital");
                    BigDecimal totalProfit = rs.getBigDecimal("totalProfit");

                    // Create a map for the month data
                    Map<String, BigDecimal> monthData = new HashMap<>();
                    monthData.put("turnover", totalTurnover);
                    monthData.put("capital", totalCapital);
                    monthData.put("profit", totalProfit);

                    // Put the month and the corresponding financial data into the map
                    financialData.put(month, monthData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return financialData;
    }

    public Map<Integer, Map<String, BigDecimal>> getStoreFinancialDataByYear(int year, int idStore) {
        Map<Integer, Map<String, BigDecimal>> financialData = new HashMap<>();
        String query = "SELECT MONTH(date) AS month, " +
                "SUM(turnover) AS totalTurnover, " +
                "SUM(capital) AS totalCapital, " +
                "SUM(profit) AS totalProfit " +
                "FROM store_financial " +
                "WHERE YEAR(date) = ? AND id_store = ? " +
                "GROUP BY MONTH(date)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, year);
            stmt.setInt(2, idStore);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("month");
                    BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                    BigDecimal totalCapital = rs.getBigDecimal("totalCapital");
                    BigDecimal totalProfit = rs.getBigDecimal("totalProfit");

                    // Create a map for the month data
                    Map<String, BigDecimal> monthData = new HashMap<>();
                    monthData.put("turnover", totalTurnover);
                    monthData.put("capital", totalCapital);
                    monthData.put("profit", totalProfit);

                    // Put the month and the corresponding financial data into the map
                    financialData.put(month, monthData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return financialData;
    }

    public Map<Integer, Map<String, BigDecimal>> getFinancialDataByMonth(int year, int month) {
        Map<Integer, Map<String, BigDecimal>> financialData = new HashMap<>();
        String query = "SELECT DAY(date) AS day, " +
                "SUM(turnover) AS totalTurnover, " +
                "SUM(capital) AS totalCapital, " +
                "SUM(profit) AS totalProfit " +
                "FROM business_financial " +
                "WHERE YEAR(date) = ? AND MONTH(date) = ? " +
                "GROUP BY DAY(date)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, year);   // Set the year parameter
            stmt.setInt(2, month);  // Set the month parameter

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int day = rs.getInt("day");
                    BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                    BigDecimal totalCapital = rs.getBigDecimal("totalCapital");
                    BigDecimal totalProfit = rs.getBigDecimal("totalProfit");

                    // Create a map for the day data
                    Map<String, BigDecimal> dayData = new HashMap<>();
                    dayData.put("turnover", totalTurnover);
                    dayData.put("capital", totalCapital);
                    dayData.put("profit", totalProfit);

                    // Put the day and the corresponding financial data into the map
                    financialData.put(day, dayData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return financialData;
    }
public Map<Integer, Map<String, BigDecimal>> getStoreFinancialDataByMonth(int year, int month, int idStore) {
        Map<Integer, Map<String, BigDecimal>> financialData = new HashMap<>();
        String query = "SELECT DAY(date) AS day, " +
                "SUM(turnover) AS totalTurnover, " +
                "SUM(capital) AS totalCapital, " +
                "SUM(profit) AS totalProfit " +
                "FROM store_financial " +
                "WHERE YEAR(date) = ? AND MONTH(date) = ? AND id_store = ? " +
                "GROUP BY DAY(date)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, year);   // Set the year parameter
            stmt.setInt(2, month);  // Set the month parameter
            stmt.setInt(3, idStore); // Set the store ID parameter

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int day = rs.getInt("day");
                    BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                    BigDecimal totalCapital = rs.getBigDecimal("totalCapital");
                    BigDecimal totalProfit = rs.getBigDecimal("totalProfit");

                    // Create a map for the day data
                    Map<String, BigDecimal> dayData = new HashMap<>();
                    dayData.put("turnover", totalTurnover);
                    dayData.put("capital", totalCapital);
                    dayData.put("profit", totalProfit);

                    // Put the day and the corresponding financial data into the map
                    financialData.put(day, dayData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return financialData;
    }

    public Map<String, BigDecimal> getTurnoverStoreData() {
        Map<String, BigDecimal> turnoverData = new HashMap<>();
        String query = "SELECT s.name AS storeName, SUM(sf.turnover) AS totalTurnover " +
                "FROM store_financial sf " +
                "JOIN stores s ON sf.id_store = s.id " +
                "GROUP BY s.name";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String storeName = rs.getString("storeName");
                BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                turnoverData.put(storeName, totalTurnover);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return turnoverData;
    }


    // Lấy tổng doanh thu theo tháng và từng cửa hàng trong một năm cụ thể
    public Map<String, BigDecimal> getTurnoverStoreDataByYear(int year) {
        Map<String, BigDecimal> turnoverData = new HashMap<>();
        String query = "SELECT s.name AS storeName, SUM(sf.turnover) AS totalTurnover " +
                "FROM store_financial sf " +
                "JOIN stores s ON sf.id_store = s.id " +
                "WHERE YEAR(sf.date) = ? " +
                "GROUP BY s.name";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String storeName = rs.getString("storeName");
                    BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                    turnoverData.put(storeName, totalTurnover);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return turnoverData;
    }


    public Map<String, BigDecimal> getTurnoverStoreDataByMonth(int year, int month) {
        Map<String, BigDecimal> turnoverData = new HashMap<>();
        String query = "SELECT s.name AS storeName, SUM(sf.turnover) AS totalTurnover " +
                "FROM store_financial sf " +
                "JOIN stores s ON sf.id_store = s.id " +
                "WHERE YEAR(sf.date) = ? AND MONTH(sf.date) = ? " +
                "GROUP BY s.name";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, year);
            stmt.setInt(2, month);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String storeName = rs.getString("storeName");
                    BigDecimal totalTurnover = rs.getBigDecimal("totalTurnover");
                    turnoverData.put(storeName, totalTurnover);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return turnoverData;
    }

    public Map<String, Integer> getRankWarehouseProduct(int idWarehouse, String rank) {
        Map<String, Integer> productData = new HashMap<>();
        String order = rank.equalsIgnoreCase("Highest") ? "DESC" : "ASC";
        String query = " SELECT p.product_name, pw.quantity FROM products_warehouse pw " +
                "JOIN products p ON pw.id_product = p.id " +
                "WHERE pw.id_warehouse = ? " +
                "ORDER BY pw.quantity " + order + " LIMIT 5";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idWarehouse);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("product_name");
                    int quantity = rs.getInt("quantity");
                    productData.put(productName, quantity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productData;
    }
    public Map<String, Integer> getRankStoreProduct(int idStore, String rank) {
        Map<String, Integer> productData = new HashMap<>();
        String order = rank.equalsIgnoreCase("Highest") ? "DESC" : "ASC";
        String query = " SELECT p.product_name, ps.quantity FROM products_store ps " +
                "JOIN products p ON ps.id_product = p.id " +
                "WHERE ps.id_store = ? " +
                "ORDER BY ps.quantity " + order + " LIMIT 5";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idStore);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("product_name");
                    int quantity = rs.getInt("quantity");
                    productData.put(productName, quantity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productData;
    }

    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();
        // Thực hiện truy vấn để lấy danh sách các năm có trong dữ liệu tài chính
        String query = "SELECT DISTINCT YEAR(date) AS year FROM business_financial"; // Cập nhật tên bảng và cột theo yêu cầu
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    years.add(resultSet.getInt("year"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return years;
    }


}
