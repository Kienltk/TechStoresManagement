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
        String storeCapitalQuery = "SELECT id_store, SUM(total) AS capital FROM import_store WHERE DATE(received_date) = CURDATE() GROUP BY id_store";
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

                // Lấy capital từ map
                double capital = capitalMap.getOrDefault(idStore, 0.0); // Mặc định capital là 0 nếu không tìm thấy trong import_store

                // Kiểm tra giá trị có giống nhau không
                if (lastTurnoverMap.getOrDefault(idStore, 0.0) == turnover &&
                        lastCapitalMap.getOrDefault(idStore, 0.0) == capital &&
                        lastProfitMap.getOrDefault(idStore, 0.0) == profit) {
                    continue; // Không thực hiện insert nếu giống
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
        String warehouseCapitalQuery = "SELECT total FROM import_warehouse WHERE DATE(product_import_date) = CURDATE() ORDER BY id DESC LIMIT 1";
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
        String query = "SELECT SUM(total) AS capital FROM import_warehouse WHERE DATE(product_import_date) = CURDATE()";
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

    public int getStock() throws SQLException {
        return calculateTotalStock();
    }

    public Map<Integer, Map<String, BigDecimal>> getFinancialDataByYear() {
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

    public Map<Integer, Map<String, BigDecimal>> getFinancialDataByMonth(int year) {
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

            stmt.setInt(1, year);  // Set the year parameter

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

    public Map<Integer, Map<String, BigDecimal>> getFinancialDataByDay(int year, int month) {
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
