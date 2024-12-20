package controller;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import view.Director;
import view.Login;
import view.buttondashboard.*;
import view.stage.LogoutFailed;
import view.stage.LogoutSuccess;


import java.util.Collection;

public class DashBoardController {
   Director director=new Director();
    @FXML
    private AnchorPane mainContent; // Khung chính để hiển thị nội dung

    @FXML
    private HBox hBoxGeneral; // HBox cho General
    @FXML
    private HBox hBoxEmployeeManagement; // HBox cho Employee Management
    @FXML
    private HBox hBoxProductManagement; // HBox cho Product Management
    @FXML
    private HBox hBoxStoreManagement; // HBox cho StoreManager Management
    @FXML
    private HBox hBoxAccount; // HBox cho Account

    @FXML
    private HBox hboxLogout;
    @FXML
    private HBox hboxWareHouse;
    @FXML
    private HBox hboxImport;
    @FXML
    private HBox hboxHistory;
    @FXML
    private Label employeeNameLabel1;



    private void clearActive() {
        hBoxGeneral.getStyleClass().remove("active");
        hBoxEmployeeManagement.getStyleClass().remove("active");
        hBoxProductManagement.getStyleClass().remove("active");
        hBoxStoreManagement.getStyleClass().remove("active");
        hBoxAccount.getStyleClass().remove("active");
        hboxLogout.getStyleClass().remove("active");
        hboxImport.getStyleClass().remove("active");
        hboxWareHouse.getStyleClass().remove("active");
        hboxHistory.getStyleClass().remove("active");
    }

    public void showLogout() {
        Session.logout();

        if (mainContent != null) {
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.close();
        } else {
            System.out.println("mainContent is null. Cannot close current stage.");
            return;
        }

        try {
            // Tạo một Stage cho pop-up LogoutSuccess
            Stage dialog = new Stage();
            LogoutSuccess logoutSuccessPopup = new LogoutSuccess();
            logoutSuccessPopup.start(dialog);

            // Lắng nghe sự kiện khi pop-up đóng
            dialog.setOnHidden(event -> {
                try {
                    new Login().start(new Stage());
                } catch (Exception e) {
                    Stage dialogFailed = new Stage();
                    LogoutFailed logoutFailedPopup = new LogoutFailed();
                    logoutFailedPopup.start(dialogFailed);
                    e.printStackTrace(); // In lỗi nếu có
                    System.out.println("Lỗi khi khởi động màn hình đăng nhập: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            Stage dialog = new Stage();
            LogoutFailed logoutFailedPopup = new LogoutFailed();
            logoutFailedPopup.start(dialog);
            e.printStackTrace(); // In lỗi nếu có
            System.out.println("Lỗi khi khởi động màn hình đăng nhập: " + e.getMessage());
        }
    }

    public void showHistory() {
        clearActive();
        hboxHistory.getStyleClass().add("active");
        HistoryView historyView = new HistoryView();
        mainContent.getChildren().setAll(historyView);
    }

    public void showGeneral() {
        clearActive();
        hBoxGeneral.getStyleClass().add("active");
        GeneralView generalView = new GeneralView();
        mainContent.getChildren().setAll(generalView);
    }

    public void showImportProduct() {
        clearActive();
        hboxImport.getStyleClass().add("active");
        ImportProductView importView = new ImportProductView();
        mainContent.getChildren().setAll(importView);
    }

    public void showWareHouse() {
        clearActive();
        hboxWareHouse.getStyleClass().add("active");
        WarehouseManagementView warehouseManagement = new WarehouseManagementView();
        mainContent.getChildren().setAll(warehouseManagement);
    }

    // Các phương thức hiển thị tab khác tương tự


    public void showEmployeeManagement() {
        clearActive();
        hBoxEmployeeManagement.getStyleClass().add("active");
        EmployeeManagementView employeeManagementView = new EmployeeManagementView();

        mainContent.getChildren().setAll(employeeManagementView);
    }

    public void showProductManagement() {
        clearActive();
        hBoxProductManagement.getStyleClass().add("active");
        ProductManagementView productManagementView = new ProductManagementView();
        mainContent.getChildren().setAll(productManagementView);
    }

    public void showStoreManagement() {
        clearActive();
        hBoxStoreManagement.getStyleClass().add("active");
        StoreManagementView storeManagementView = new StoreManagementView();
        mainContent.getChildren().setAll(storeManagementView);
    }

    public void showAccount() {
        clearActive();
        hBoxAccount.getStyleClass().add("active");
        AccountView accountView = new AccountView();
        mainContent.getChildren().setAll(accountView);
    }

    // Phương thức khởi tạo (initialize) sẽ được gọi khi controller được tạo
    @FXML
    public void initialize() {
        // Gọi showGeneral() để mặc định tab General được active
        employeeNameLabel1.setText(director.getEmployeeName());
        showGeneral();
    }
}
