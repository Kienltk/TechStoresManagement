package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
    private HBox hBoxAccount; // HBox cho Settings

    // Phương thức để xóa trạng thái active từ tất cả các tab
    private void clearActive() {
        hBoxGeneral.getStyleClass().remove("active");
        hBoxEmployeeManagement.getStyleClass().remove("active");
        hBoxProductManagement.getStyleClass().remove("active");
        hBoxStoreManagement.getStyleClass().remove("active");
        hBoxAccount.getStyleClass().remove("active");
    }

    // Phương thức hiển thị tab General
    public void showGeneral() {
        clearActive();
        hBoxGeneral.getStyleClass().add("active");
        GeneralView generalView = new GeneralView();
        mainContent.getChildren().setAll(generalView);
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
        AccountView AccountView = new AccountView();
        mainContent.getChildren().setAll(AccountView);
    }

    // Phương thức khởi tạo (initialize) sẽ được gọi khi controller được tạo
    @FXML
    public void initialize() {
        // Gọi showGeneral() để mặc định tab General được active
        showGeneral();
    }
}
