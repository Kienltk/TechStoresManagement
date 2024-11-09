package controller;

import model.GeneralModel;

import java.sql.SQLException;

public class GeneralController {

    private GeneralModel model = new GeneralModel();

    public void handleReloadDirector() {
        try {
            model.insertStoreFinancialData();
            model.insertBusinessFinancialData();
            model.calculateTotalStock();
            System.out.println("Reload complete. Data updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleReloadDWarehouse(int idWarehouse) {
            model.getStockWarehouse(idWarehouse);
            System.out.println("Reload complete. Data updated.");
    }


    public double getTurnover() {
        try {
            return model.getTurnover();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getCapital() {
        try {
            return model.getCapital();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getProfit() {
        try {
            return model.getProfit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getStock() {
        try {
            return model.getStock();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getStockWarehouse(int idWarehouse) {
        return model.getStockWarehouse(idWarehouse);
    }

}
