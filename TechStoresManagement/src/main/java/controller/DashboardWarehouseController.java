package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import view.Login;
import view.WarehouseManager;
import view.buttondashboardwarehouse.EmployeeView;
import view.buttondashboardwarehouse.GeneralView;
import view.buttondashboardwarehouse.ImportView;
import view.buttondashboardwarehouse.ProductView;
import view.stage.LogoutFailed;
import view.stage.LogoutSuccess;

public class DashboardWarehouseController {
    WarehouseManager warehouse = new WarehouseManager();


    @FXML
    private AnchorPane mainContent;

    @FXML
    private HBox general; // HBox cho General
    @FXML
    private HBox employee; // HBox cho Employee Management
    @FXML
    private HBox product; // HBox cho Product Management

    @FXML
    private HBox logout;

    @FXML
    private HBox importProduct;

    @FXML
    private Label employeeNameLabel;

    private void clearActive() {
        general.getStyleClass().remove("active");
        employee.getStyleClass().remove("active");
        product.getStyleClass().remove("active");
        logout.getStyleClass().remove("active");
        importProduct.getStyleClass().remove("active");
    }

    public void showLogoutWarehouse() {
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



    public void showGeneralWarehouse() {
        clearActive();
        general.getStyleClass().add("active");
        GeneralView generalView = new GeneralView();
        mainContent.getChildren().setAll(generalView);
    }

    public void showImportWarehouse() {
        clearActive();
        importProduct.getStyleClass().add("active");
        ImportView importView = new ImportView();
        mainContent.getChildren().setAll(importView);
    }
    // Các phương thức hiển thị tab khác tương tự


    public void showEmployeeWarehouse() {
        clearActive();
        employee.getStyleClass().add("active");
        EmployeeView employeeManagementView = new EmployeeView();

        mainContent.getChildren().setAll(employeeManagementView);
    }

    public void showProductWarehouse() {
        clearActive();
        product.getStyleClass().add("active");
        ProductView productManagementView = new ProductView();
        mainContent.getChildren().setAll(productManagementView);
    }


    // Phương thức khởi tạo (initialize) sẽ được gọi khi controller được tạo
    @FXML
    public void initialize() {
        System.out.println(warehouse.getEmployeeName());
        employeeNameLabel.setText(warehouse.getEmployeeName());
        showGeneralWarehouse();
    }
}
