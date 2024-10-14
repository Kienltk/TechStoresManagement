package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import view.Login;
import view.buttondashboard.*;

import java.util.Collection;

public class DashBoardController {

    @FXML
    private AnchorPane mainContent; // Khung chính để hiển thị nội dung

    @FXML
    private HBox hBoxGeneral; // HBox cho General
    @FXML
    private HBox hBoxEmployeeManagement; // HBox cho Employee Management
    @FXML
    private HBox hBoxProductManagement; // HBox cho Product Management
    @FXML
    private HBox hBoxStoreManagement; // HBox cho Store Management
    @FXML
    private HBox hBoxAccount; // HBox cho Account

    @FXML
    private HBox hboxLogout;
    @FXML
    private HBox hboxWareHouse;
    @FXML
    private HBox hboxImport;



    // Phương thức để xóa trạng thái active từ tất cả các tab
    private void clearActive() {
        hBoxGeneral.getStyleClass().remove("active");
        hBoxEmployeeManagement.getStyleClass().remove("active");
        hBoxProductManagement.getStyleClass().remove("active");
        hBoxStoreManagement.getStyleClass().remove("active");
        hBoxAccount.getStyleClass().remove("active");
        hboxLogout.getStyleClass().remove("active");
        hboxImport.getStyleClass().remove("active");
        hboxWareHouse.getStyleClass().remove("active");
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
            new Login().start(new Stage());
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi nếu có
            System.out.println("Lỗi khi khởi động màn hình đăng nhập: " + e.getMessage());
        }
    }



    public void showGeneral() {
        clearActive();
        hBoxGeneral.getStyleClass().add("active");
        GeneralView generalView = new GeneralView();
        mainContent.getChildren().setAll(generalView);
    }
    public void showImportProduct(){
        clearActive();
        hboxImport.getStyleClass().add("active");
        ImportProductView importView = new ImportProductView();
        mainContent.getChildren().setAll(importView);
    }
public void showWareHouse(){
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
        showGeneral();
    }
}
