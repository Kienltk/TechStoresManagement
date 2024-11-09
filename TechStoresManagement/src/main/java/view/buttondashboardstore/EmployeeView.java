package view.buttondashboardstore;

import controller.Session;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import entity.Employee;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.EmployeeModel;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EmployeeView extends VBox {
    private final int idStore = Session.getIdWarehouse();

    private TableView<Employee> tableView;
    private ObservableList<Employee> employeeList;
    private EmployeeModel employeeModel;
    private TextField searchField;
    private int currentPage = 1;
    private final int itemsPerPage = 12;
    private int totalPages;
    private final Label pageLabel = new Label();

    public EmployeeView() {
        employeeModel = new EmployeeModel();
        employeeList = FXCollections.observableArrayList();
        loadEmployees();
        // Title Label
        Label titleLabel = new Label("Employee Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");


        tableView = new TableView<>();
        tableView.setItems(employeeList);
        tableView.getStyleClass().add("table-view");
        configureTableView();

        searchField = new TextField();
        searchField.setPromptText("Search by Name");
        searchField.getStyleClass().add("search-box");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchEmployees(newValue));

        HBox searchBar = new HBox(searchField);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setStyle(" -fx-padding:0 610 10 0;");

        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll(searchBar);

        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("button-pagination");
        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("button-pagination");
        pageLabel.getStyleClass().add("text-pagination");

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableData();
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateTableData();
            }
        });

        HBox paginationBox = new HBox(10, prevButton, pageLabel, nextButton);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setSpacing(30);
        paginationBox.setStyle("-fx-padding: 8");

        // Add everything to main layout
        this.getStyleClass().add("vbox");
        VBox vbox = new VBox(titleLabel,topControls, tableView,paginationBox);
        this.getChildren().addAll(vbox);
    }


    private void configureTableView() {
        TableColumn<Employee, Number> sttCol = new TableColumn<>("No.");
        sttCol.setMinWidth(70);
        sttCol.getStyleClass().add("column");
        sttCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(cellData.getValue()) + 1));
        sttCol.setSortable(false);

        TableColumn<Employee, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setMinWidth(235);
        fullNameCol.getStyleClass().add("column");
        fullNameCol.setCellValueFactory(data -> data.getValue().firstNameProperty().concat(" ").concat(data.getValue().lastNameProperty()));

        TableColumn<Employee, String> genderCol = new TableColumn<>("Gender");
        genderCol.setMinWidth(100);
        genderCol.getStyleClass().add("column");
        genderCol.setCellValueFactory(data -> {
            boolean isMale = data.getValue().isGender();
            return new SimpleStringProperty(isMale ? "Male" : "Female");
        });
        TableColumn<Employee, LocalDate> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setMinWidth(100);
        dobCol.getStyleClass().add("column");
        dobCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDob().toLocalDate()));

        TableColumn<Employee, String> emailCol = new TableColumn<>("Email");
        emailCol.setMinWidth(200);
        emailCol.getStyleClass().add("column");
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<Employee, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setMinWidth(200);
        phoneCol.getStyleClass().add("column");
        phoneCol.setCellValueFactory(data -> data.getValue().phoneNumberProperty());

        TableColumn<Employee, String> roleCol = new TableColumn<>("Role");
        roleCol.setMinWidth(220);
        roleCol.getStyleClass().add("column");
        roleCol.setCellValueFactory(data -> data.getValue().roleProperty());

        TableColumn<Employee, String> workplaceCol = new TableColumn<>("Workplace");
        workplaceCol.setMinWidth(220);
        workplaceCol.getStyleClass().add("column");
        workplaceCol.setCellValueFactory(data -> data.getValue().workplaceProperty());

        TableColumn<Employee, Double> salaryCol = new TableColumn<>("Salary");
        salaryCol.setMinWidth(100);
        salaryCol.getStyleClass().add("column");
        salaryCol.setCellValueFactory(data -> data.getValue().salaryProperty().asObject());

        TableColumn<Employee, String> actionCol = new TableColumn<>("        Action");
        actionCol.getStyleClass().add("column");
        actionCol.setMinWidth(200);

        actionCol.setCellFactory(col -> new TableCell<Employee, String>() {
            private final Button viewButton = new Button();

            {
                // Tạo ImageView cho các icon
                ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));

                // Đặt kích thước ban đầu cho icon
                setIconSize(viewIcon, 20);

                // Thêm icon vào nút
                viewButton.setGraphic(viewIcon);

                // Đặt style cho nút
                String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;";
                viewButton.setStyle(defaultStyle);

                // Thêm sự kiện phóng to khi hover và giảm padding
                addHoverEffect(viewButton, viewIcon);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); // Không hiển thị gì nếu hàng trống
                } else {
                    Employee employee = getTableView().getItems().get(getIndex());

                    // Đặt hành động cho nút
                    viewButton.setOnAction(e -> openViewEmployeeDialog(employee));

                    HBox buttons = new HBox(viewButton);
                    buttons.setStyle("-fx-alignment: CENTER_LEFT; -fx-spacing: 10;");
                    setGraphic(buttons);
                }
            }

            // Phương thức thiết lập kích thước cho ImageView
            private void setIconSize(ImageView icon, int size) {
                icon.setFitWidth(size);
                icon.setFitHeight(size);
            }

            // Phương thức thêm hiệu ứng hover
            private void addHoverEffect(Button button, ImageView icon) {
                button.setOnMouseEntered(e -> {
                    setIconSize(icon, 25); // Phóng to khi hover
                    button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 3.2;"); // Giảm padding khi hover
                });

                button.setOnMouseExited(e -> {
                    setIconSize(icon, 20); // Trở lại kích thước ban đầu khi rời chuột
                    button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;"); // Khôi phục padding ban đầu
                });
            }
        });

        tableView.getColumns().addAll(sttCol, fullNameCol, genderCol, roleCol, workplaceCol, actionCol);
    }

    private void loadEmployees() {
        try {
            employeeList.clear(); // Xóa danh sách cũ
            List<Employee> employees = employeeModel.getAllEmployeesByWarehouse(idStore);
            totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);

            // Get the sublist for the current page
            int fromIndex = (currentPage - 1) * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, employees.size());
            List<Employee> paginatedEmployees = employees.subList(fromIndex, toIndex);

            employeeList.addAll(paginatedEmployees);
            pageLabel.setText("Page " + currentPage + " / " + totalPages); // Update the page label
        } catch (Exception e) {
            showError("Failed to load employees: " + e.getMessage());
        }
    }

    private void searchEmployees(String name) {
        employeeList.clear();
        try {
            List<Employee> employees = employeeModel.searchEmployeesByName(name);
            totalPages = (int) Math.ceil((double) employees.size() / itemsPerPage);

            // Get the sublist for the current page
            int fromIndex = (currentPage - 1) * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, employees.size());
            List<Employee> paginatedEmployees = employees.subList(fromIndex, toIndex);

            employeeList.addAll(paginatedEmployees);
            pageLabel.setText("Page " + currentPage + " / " + totalPages); // Update the page label
        } catch (SQLException e) {
            showError("Failed to search employees: " + e.getMessage());
        }
    }

    private void updateTableData() {
        loadEmployees(); // Load employees for the current page
    };

    private void openViewEmployeeDialog(Employee employee) {
        Stage dialog = new Stage();
        dialog.setTitle("View Employee");
        dialog.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Create Labels with the variable data
        Label firstNameLabel = new Label("First Name:");
        Label firstNameData = new Label(employee.getFirstName());
        firstNameLabel.getStyleClass().add("label-popup");
        firstNameData.getStyleClass().add("data-popup");

        Label lastNameLabel = new Label("Last Name:");
        Label lastNameData = new Label(employee.getLastName());
        lastNameLabel.getStyleClass().add("label-popup");
        lastNameData.getStyleClass().add("data-popup");

        Label genderLabel = new Label("Gender:");
        Label genderData = new Label(employee.isGender() ? "Male" : "Female");
        genderLabel.getStyleClass().add("label-popup");
        genderData.getStyleClass().add("data-popup");

        Label dobLabel = new Label("Date of Birth:");
        Label dobData = new Label(employee.getDob().toString()); // Chuyển đổi sang chuỗi
        dobLabel.getStyleClass().add("label-popup");
        dobData.getStyleClass().add("data-popup");

        Label emailLabel = new Label("Email:");
        Label emailData = new Label(employee.getEmail());
        emailLabel.getStyleClass().add("label-popup");
        emailData.getStyleClass().add("data-popup");

        Label phoneLabel = new Label("Phone Number:");
        Label phoneData = new Label(employee.getPhoneNumber());
        phoneLabel.getStyleClass().add("label-popup");
        phoneData.getStyleClass().add("data-popup");

        Label addressLabel = new Label("Address:");
        Label addressData = new Label(employee.getAddress());
        addressLabel.getStyleClass().add("label-popup");
        addressData.getStyleClass().add("data-popup");

        Label salaryLabel = new Label("Salary:");
        Label salaryData = new Label(String.valueOf(employee.getSalary()));
        salaryLabel.getStyleClass().add("label-popup");
        salaryData.getStyleClass().add("data-popup");

        Label roleLabel = new Label("Role:");
        Label roleData = new Label(employee.getRole());
        roleLabel.getStyleClass().add("label-popup");
        roleData.getStyleClass().add("data-popup");

        Label workplaceLabel = new Label("Workplace:");
        Label workplaceData = new Label(employee.getWorkplace());
        workplaceLabel.getStyleClass().add("label-popup");
        workplaceData.getStyleClass().add("data-popup");
        // Add labels and data to the grid
        grid.add(firstNameLabel, 0, 0);
        grid.add(firstNameData, 1, 0);

        grid.add(lastNameLabel, 0, 1);
        grid.add(lastNameData, 1, 1);

        grid.add(genderLabel, 0, 2);
        grid.add(genderData, 1, 2);

        grid.add(dobLabel, 0, 3);
        grid.add(dobData, 1, 3);

        grid.add(emailLabel, 0, 4);
        grid.add(emailData, 1, 4);

        grid.add(phoneLabel, 0, 5);
        grid.add(phoneData, 1, 5);

        grid.add(addressLabel, 0, 6);
        grid.add(addressData, 1, 6);

        grid.add(salaryLabel, 0, 7);
        grid.add(salaryData, 1, 7);

        grid.add(roleLabel, 0, 8);
        grid.add(roleData, 1, 8);

        grid.add(workplaceLabel, 0, 9);
        grid.add(workplaceData, 1, 9);

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setAlignment(Pos.CENTER_RIGHT);
        closeButton.getStyleClass().add("button-pagination");
        closeButton.setOnAction(e -> dialog.close());

        // Add the grid and button to the VBox
        VBox vbox = new VBox(grid, closeButton);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(15));

        Scene dialogScene = new Scene(vbox);
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialogScene.getStylesheets().add(getClass().getResource("/view/director.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}