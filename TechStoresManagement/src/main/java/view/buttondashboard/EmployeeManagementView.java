package view.buttondashboard;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import entity.Employee;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.EmployeeModel;
import view.stage.DeletionFailed;
import view.stage.DeletionSuccess;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;


public class EmployeeManagementView extends VBox {

    private TableView<Employee> tableView;
    private ObservableList<Employee> employeeList;
    private EmployeeModel employeeModel;
    private TextField searchField;
    private int currentPage = 1;
    private final int itemsPerPage = 12;
    private int totalPages;
    private final Label pageLabel = new Label();


    public EmployeeManagementView() {
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

        Button addButton = new Button("Add Employee");
        addButton.getStyleClass().add("button-pagination");
        addButton.setOnAction(e -> openAddEmployeeDialog());

        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll(searchBar, addButton);



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
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                // Tạo ImageView cho các icon
                ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));

                // Đặt kích thước ban đầu cho icon
                setIconSize(viewIcon, 20);
                setIconSize(editIcon, 20);
                setIconSize(deleteIcon, 20);

                // Thêm icon vào nút
                viewButton.setGraphic(viewIcon);
                editButton.setGraphic(editIcon);
                deleteButton.setGraphic(deleteIcon);

                // Đặt style cho nút
                String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;";
                viewButton.setStyle(defaultStyle);
                editButton.setStyle(defaultStyle);
                deleteButton.setStyle(defaultStyle);

                // Thêm sự kiện phóng to khi hover và giảm padding
                addHoverEffect(viewButton, viewIcon);
                addHoverEffect(editButton, editIcon);
                addHoverEffect(deleteButton, deleteIcon);
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
                    editButton.setOnAction(e -> openEditEmployeeDialog(employee));
                    deleteButton.setOnAction(e -> deleteEmployee(employee));

                    HBox buttons = new HBox(viewButton, editButton, deleteButton);
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
            List<Employee> employees = employeeModel.getAllEmployees();
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
    private void openAddEmployeeDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add Employee");
        dialog.initModality(Modality.APPLICATION_MODAL);

        GridPane dialogLayout = new GridPane();
        dialogLayout.setPadding(new Insets(0 ,10,0,10));
        dialogLayout.setVgap(10);
        dialogLayout.setHgap(10);

        // Khai báo các trường nhập liệu
        TextField firstNameField = new TextField();
        firstNameField.getStyleClass().add("text-field-account");
        firstNameField.setPromptText("First Name");
        Label firstNameError = new Label();
        firstNameError.setTextFill(Color.RED);
        firstNameError.setVisible(false);
        firstNameError.setMinHeight(5); // Đặt chiều cao tối thiểu

        TextField lastNameField = new TextField();
        lastNameField.getStyleClass().add("text-field-account");
        lastNameField.setPromptText("Last Name");
        Label lastNameError = new Label();
        lastNameError.setTextFill(Color.RED);
        lastNameError.setVisible(false);
        lastNameError.setMinHeight(5); // Đặt chiều cao tối thiểu

        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        maleRadio.getStyleClass().add("radio-button-account");
        RadioButton femaleRadio = new RadioButton("Female");
        femaleRadio.getStyleClass().add("radio-button-account");
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);

        TextField dobField = new TextField();
        dobField.getStyleClass().add("text-field-account");
        dobField.setPromptText("DD/MM/YYYY");
        Label dobError = new Label();
        dobError.setTextFill(Color.RED);
        dobError.setVisible(false);
        dobError.setMinHeight(5); // Đặt chiều cao tối thiểu

        // Listener cho định dạng ngày sinh
        dobField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 2 || newValue.length() == 5) {
                dobField.setText(newValue + "/");
                dobField.positionCaret(newValue.length() + 1);
            }
        });

        TextField emailField = new TextField();
        emailField.getStyleClass().add("text-field-account");
        emailField.setPromptText("Email");
        Label emailError = new Label();
        emailError.setTextFill(Color.RED);
        emailError.setVisible(false);
        emailError.setMinHeight(5); // Đặt chiều cao tối thiểu

        TextField phoneField = new TextField();
        phoneField.getStyleClass().add("text-field-account");
        phoneField.setPromptText("Phone Number");
        Label phoneError = new Label();
        phoneError.setTextFill(Color.RED);
        phoneError.setVisible(false);
        phoneError.setMinHeight(5); // Đặt chiều cao tối thiểu

        TextField addressField = new TextField();
        addressField.getStyleClass().add("text-field-account");
        addressField.setPromptText("Address");

        TextField salaryField = new TextField();
        salaryField.getStyleClass().add("text-field-account");
        salaryField.setPromptText("Salary");
        Label salaryError = new Label();
        salaryError.setTextFill(Color.RED);
        salaryError.setVisible(false);
        salaryError.setMinHeight(5); // Đặt chiều cao tối thiểu

        // Thêm listener cho từng trường để kiểm tra và hiển thị thông báo lỗi ngay khi nhập
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                firstNameError.setText("First Name is required.");
                firstNameError.setVisible(true);
            } else if (!newValue.matches("[a-zA-Z]+")) {
                firstNameError.setText("First Name must not contain numbers.");
                firstNameError.setVisible(true);
            } else {
                firstNameError.setVisible(false);
            }
        });

        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                lastNameError.setText("Last Name is required.");
                lastNameError.setVisible(true);
            } else if (!newValue.matches("[a-zA-Z]+")) {
                lastNameError.setText("Last Name must not contain numbers.");
                lastNameError.setVisible(true);
            } else {
                lastNameError.setVisible(false);
            }
        });

        dobField.textProperty().addListener(( observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                dobError.setText("Date of Birth is required.");
                dobError.setVisible(true);
            } else {
                try {
                    LocalDate dob = LocalDate.parse(newValue, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    if (Period.between(dob, LocalDate.now()).getYears() < 18) {
                        dobError.setText("You must be at least 18 years old.");
                        dobError.setVisible(true);
                    } else {
                        dobError.setVisible(false);
                    }
                } catch (DateTimeParseException ex) {
                    dobError.setText("Invalid date format. Use DD/MM/YYYY.");
                    dobError.setVisible(true);
                }
            }
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                emailError.setText("Email is required.");
                emailError.setVisible(true);
            } else if (!newValue.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                emailError.setText("Invalid email format.");
                emailError.setVisible(true);
            } else {
                emailError.setVisible(false);
            }
        });

        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                phoneError.setText("Phone Number is required.");
                phoneError.setVisible(true);
            } else if (!newValue.matches("\\d+")) {
                phoneError.setText("Phone Number must be numeric.");
                phoneError.setVisible(true);
            } else {
                phoneError.setVisible(false);
            }
        });

        salaryField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                salaryError.setText("Salary is required.");
                salaryError.setVisible(true);
            } else {
                try {
                    Double.parseDouble(newValue);
                    salaryError.setVisible(false);
                } catch (NumberFormatException ex) {
                    salaryError.setText("Salary must be a valid number.");
                    salaryError.setVisible(true);
                }
            }
        });

        ComboBox<Pair<Integer, String>> roleComboBox = new ComboBox<>();
        roleComboBox.getStyleClass().add("combo-box-account");
        ComboBox<Pair<Integer, String>> storeComboBox = new ComboBox<>();
        storeComboBox.getStyleClass().add("combo-box-account");
        ComboBox<Pair<Integer, String>> warehouseComboBox = new ComboBox<>();
        warehouseComboBox.getStyleClass().add("combo-box-account");

        Label storeManagerLabel = new Label("Store:");
        Label warehouseLabel = new Label("Warehouse:");
        // Load roles từ cơ sở dữ liệu
        loadRoles(roleComboBox);

        // Ẩn các Label và ComboBox cho Store và Warehouse ban đầu
        storeManagerLabel.setVisible(false);
        storeComboBox.setVisible(false);
        warehouseLabel.setVisible(false);
        warehouseComboBox.setVisible(false);
        storeComboBox.managedProperty().bind(storeComboBox.visibleProperty());
        warehouseComboBox.managedProperty().bind(warehouseComboBox.visibleProperty());

        // Thay đổi hiển thị của ComboBox dựa trên vai trò đã chọn
        loadStores(storeComboBox);
        loadWarehouses(warehouseComboBox);
        roleComboBox.setOnAction(event -> {
            String selectedRole = roleComboBox.getValue() != null ? roleComboBox.getValue().getValue() : null;

            if ("Store Management".equals(selectedRole) || "Cashier".equals(selectedRole)) {
                storeManagerLabel.setVisible(true);
                storeComboBox.setVisible(true);
                warehouseLabel.setVisible(false);
                warehouseComboBox.setVisible(false);
            } else if ("Warehouse Management".equals(selectedRole)) {
                storeManagerLabel.setVisible(false);
                storeComboBox.setVisible(false);
                warehouseLabel.setVisible(true);
                warehouseComboBox.setVisible(true);
            } else {
                storeManagerLabel.setVisible(false);
                storeComboBox.setVisible(false);
                warehouseLabel.setVisible(false);
                warehouseComboBox.setVisible(false);
            }
        });

        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("button-account");
        submitButton.setOnAction(e -> {
            firstNameError.setVisible(false);
            lastNameError.setVisible(false);
            dobError.setVisible(false);
            emailError.setVisible(false);
            phoneError.setVisible(false);
            salaryError.setVisible(false);

            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String dobText = dobField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String salaryText = salaryField.getText();

            boolean valid = true;

            // Validate first name
            if (firstName.isEmpty()) {
                firstNameError.setText("First Name is required.");
                firstNameError.setVisible(true);
                valid = false;
            } else if (!firstName.matches("[a-zA -Z]+")) {
                firstNameError.setText("First Name must not contain numbers.");
                firstNameError.setVisible(true);
                valid = false;
            }

            // Validate last name
            if (lastName.isEmpty()) {
                lastNameError.setText("Last Name is required.");
                lastNameError.setVisible(true);
                valid = false;
            } else if (!lastName.matches("[a-zA-Z]+")) {
                lastNameError.setText("Last Name must not contain numbers.");
                lastNameError.setVisible(true);
                valid = false;
            }

            // Validate Date of Birth
            if (dobText.isEmpty()) {
                dobError.setText("Date of Birth is required.");
                dobError.setVisible(true);
                valid = false;
            } else {
                try {
                    LocalDate dob = LocalDate.parse(dobText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    if (Period.between(dob, LocalDate.now()).getYears() < 18) {
                        dobError.setText("You must be at least 18 years old.");
                        dobError.setVisible(true);
                        valid = false;
                    }
                } catch (DateTimeParseException ex) {
                    dobError.setText("Invalid date format. Use DD/MM/YYYY.");
                    dobError.setVisible(true);
                    valid = false;
                }
            }

            // Validate email format
            if (email.isEmpty()) {
                emailError.setText("Email is required.");
                emailError.setVisible(true);
                valid = false;
            } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                emailError.setText("Invalid email format.");
                emailError.setVisible(true);
                valid = false;
            }

            // Validate phone number
            if (phone.isEmpty()) {
                phoneError.setText("Phone Number is required.");
                phoneError.setVisible(true);
                valid = false;
            } else if (!phone.matches("\\d+")) {
                phoneError.setText("Phone Number must be numeric.");
                phoneError.setVisible(true);
                valid = false;
            }

            // Validate salary
            if (salaryText.isEmpty()) {
                salaryError.setText("Salary is required.");
                salaryError.setVisible(true);
                valid = false;
            } else {
                try {
                    Double.parseDouble(salaryText);
                } catch (NumberFormatException ex) {
                    salaryError.setText("Salary must be a valid number.");
                    salaryError.setVisible(true);
                    valid = false;
                }
            }

            if (!valid) return;

            Employee newEmployee = new Employee();
            newEmployee.setFirstName(firstName);
            newEmployee.setLastName(lastName);
            LocalDate dob = LocalDate.parse(dobText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            newEmployee.setDob(java.sql.Date.valueOf(dob));
            newEmployee.setEmail(email);
            newEmployee.setPhoneNumber(phone);
            newEmployee.setAddress(addressField.getText());
            newEmployee.setSalary(Double.parseDouble(salaryText));
            newEmployee.setGender(maleRadio.isSelected());

            if (roleComboBox.getValue() != null) {
                newEmployee.setIdRole(roleComboBox.getValue().getKey());
            }

            int selectedStoreId = storeComboBox.getValue() != null ? storeComboBox.getValue().getKey() : 0;
            int selectedWarehouseId = warehouseComboBox.getValue() != null ? warehouseComboBox.getValue().getKey() : 0;

            newEmployee.setIdStore(selectedStoreId);
            newEmployee.setIdWarehouse(selectedWarehouseId);

            try {
                employeeModel.insertEmployee(newEmployee);
                showSuccess("Employee added successfully.");
                loadEmployees();
                dialog.close();
            } catch (Exception ex) {
                showError("Failed to add employee: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-cancel-account");
        cancelButton.setOnAction(e -> dialog.close());

        // Add fields to the grid using VBox for error labels
// Khai báo các biến Label
        Label firstNameLabel = new Label("First Name:");
        Label lastNameLabel = new Label("Last Name:");
        Label genderLabel = new Label("Gender:");
        Label dobLabel = new Label("Date of Birth:");
        Label emailLabel = new Label("Email:");
        Label phoneLabel = new Label("Phone Number:");
        Label addressLabel = new Label("Address:");
        Label salaryLabel = new Label("Salary:");
        Label roleLabel = new Label("Role:");

// Thêm class CSS cho các Label
        firstNameLabel.getStyleClass().add("label-popup");
        lastNameLabel.getStyleClass().add("label-popup");
        genderLabel.getStyleClass().add("label-popup");
        dobLabel.getStyleClass().add("label-popup");
        emailLabel.getStyleClass().add("label-popup");
        phoneLabel.getStyleClass().add("label-popup");
        addressLabel.getStyleClass().add("label-popup");
        salaryLabel.getStyleClass().add("label-popup");
        roleLabel.getStyleClass().add("label-popup");
        storeManagerLabel.getStyleClass().add("label-popup");
        warehouseLabel.getStyleClass().add("label-popup");

// Thêm các Label vào dialogLayout
        dialogLayout.add(firstNameLabel, 0, 0);
        dialogLayout.add(firstNameField, 1, 0);
        dialogLayout.add(firstNameError, 1, 1);

        dialogLayout.add(lastNameLabel, 0, 2);
        dialogLayout.add(lastNameField, 1, 2);
        dialogLayout.add(lastNameError, 1, 3);

        dialogLayout.add(genderLabel, 0, 4);
        dialogLayout.add(maleRadio, 1, 4);
        dialogLayout.add(femaleRadio, 1, 5);

        dialogLayout.add(dobLabel, 0, 7);
        dialogLayout.add(dobField, 1, 7);
        dialogLayout.add(dobError, 1, 8);

        dialogLayout.add(emailLabel, 0, 9);
        dialogLayout.add(emailField, 1, 9);
        dialogLayout.add(emailError, 1, 10);

        dialogLayout.add(phoneLabel, 0, 11);
        dialogLayout.add(phoneField, 1, 11);
        dialogLayout.add(phoneError, 1, 12);

        dialogLayout.add(addressLabel, 0, 13);
        dialogLayout.add(addressField, 1, 13);

        dialogLayout.add(salaryLabel, 0, 15);
        dialogLayout.add(salaryField, 1, 15);
        dialogLayout.add(salaryError, 1, 16);

        dialogLayout.add(roleLabel, 0, 17);
        dialogLayout.add(roleComboBox, 1, 17);

        dialogLayout.add(storeManagerLabel, 0, 19);
        dialogLayout.add(storeComboBox, 1, 19);

        dialogLayout.add(warehouseLabel, 0, 19);
        dialogLayout.add(warehouseComboBox, 1, 19);

        dialogLayout.add(submitButton, 0, 20);
        dialogLayout.add(cancelButton, 1, 20);

        Scene dialogScene = new Scene(dialogLayout, 500, 720);
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }


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

    private void openEditEmployeeDialog(Employee employee) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Employee");
        dialog.initModality(Modality.APPLICATION_MODAL);

        GridPane dialogLayout = new GridPane();
        dialogLayout.setPadding(new Insets(10));
        dialogLayout.setVgap(10);
        dialogLayout.setHgap(10);

        TextField firstNameField = new TextField(employee.getFirstName());
        firstNameField.getStyleClass().add("text-field-account");

        TextField lastNameField = new TextField(employee.getLastName());
        lastNameField.getStyleClass().add("text-field-account");

        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        maleRadio.getStyleClass().add("radio-button-account");

        RadioButton femaleRadio = new RadioButton("Female");
        femaleRadio.getStyleClass().add("radio-button-account");
        if (employee.isGender()) {
            maleRadio.setSelected(true);
        } else {
            femaleRadio.setSelected(true);
        }
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);

        DatePicker dobPicker = new DatePicker(employee.getDob().toLocalDate());
        dobPicker.getStyleClass().add("text-field-account");

        TextField emailField = new TextField(employee.getEmail());
        emailField.getStyleClass().add("text-field-account");

        TextField phoneField = new TextField(employee.getPhoneNumber());
        phoneField.getStyleClass().add("text-field-account");

        TextField addressField = new TextField(employee.getAddress());
        addressField.getStyleClass().add("text-field-account");

        TextField salaryField = new TextField(String.valueOf(employee.getSalary()));
        salaryField.getStyleClass().add("text-field-account");

        ComboBox<Pair<Integer, String>> roleComboBox = new ComboBox<>();
        roleComboBox.getStyleClass().add("combo-box-account");
        ComboBox<Pair<Integer, String>> storeComboBox = new ComboBox<>();
        storeComboBox.getStyleClass().add("combo-box-account");
        ComboBox<Pair<Integer, String>> warehouseComboBox = new ComboBox<>();
        warehouseComboBox.getStyleClass().add("combo-box-account");

        // Add fields to the grid
// Khai báo các biến Label
        Label firstNameLabel = new Label("First Name:");
        Label lastNameLabel = new Label("Last Name:");
        Label genderLabel = new Label("Gender:");
        Label dobLabel = new Label("Date of Birth:");
        Label emailLabel = new Label("Email:");
        Label phoneLabel = new Label("Phone Number:");
        Label addressLabel = new Label("Address:");
        Label salaryLabel = new Label("Salary:");
        Label roleLabel = new Label("Role:");
        Label storeManagerLabel = new Label("Store:");
        Label warehouseLabel = new Label("Warehouse:");

// Thêm class CSS cho các Label
        firstNameLabel.getStyleClass().add("label-popup");
        lastNameLabel.getStyleClass().add("label-popup");
        genderLabel.getStyleClass().add("label-popup");
        dobLabel.getStyleClass().add("label-popup");
        emailLabel.getStyleClass().add("label-popup");
        phoneLabel.getStyleClass().add("label-popup");
        addressLabel.getStyleClass().add("label-popup");
        salaryLabel.getStyleClass().add("label-popup");
        roleLabel.getStyleClass().add("label-popup");
        storeManagerLabel.getStyleClass().add("label-popup");
        warehouseLabel.getStyleClass().add("label-popup");

        // Load roles from the database
        loadRoles(roleComboBox);

        // Set selected role
        roleComboBox.setValue(new Pair<>(employee.getIdRole(), employee.getRole()));
        // Ẩn các Label và ComboBox cho Store và Warehouse ban đầu
        storeManagerLabel.setVisible(false);
        storeComboBox.setVisible(false);
        warehouseLabel.setVisible(false);
        warehouseComboBox.setVisible(false);
        storeComboBox.setValue(null); // Hoặc một Pair hợp lệ nếu có
        // Thay đổi ComboBox hiển thị theo vai trò
        roleComboBox.setOnAction(event -> {
            String selectedRole = roleComboBox.getValue() != null ? roleComboBox.getValue().getValue() : null;
            if ("Store Management".equals(selectedRole)|| "Cashier".equals(selectedRole)) {
                storeManagerLabel.setVisible(true);
                storeComboBox.setVisible(true);
                warehouseLabel.setVisible(false);
                warehouseComboBox.setVisible(false);
                storeComboBox.setValue(new Pair<>(employee.getIdStore(), employeeModel.getStoreNameById(employee.getIdStore())));
                loadStores(storeComboBox); // Load stores
            } else if ("Warehouse Management".equals(selectedRole)) {
                storeManagerLabel.setVisible(false);
                storeComboBox.setVisible(false);
                warehouseLabel.setVisible(true);
                warehouseComboBox.setVisible(true);
                warehouseComboBox.setValue(new Pair<>(employee.getIdWarehouse(), employeeModel.getWarehouseNameById(employee.getIdWarehouse())));
                loadWarehouses(warehouseComboBox); // Load warehouses
            } else {
                storeManagerLabel.setVisible(false);
                storeComboBox.setVisible(false);
                warehouseLabel.setVisible(false);
                warehouseComboBox.setVisible(false);
            }
        });

// Kích hoạt lần đầu
        roleComboBox.fireEvent(new ActionEvent());

        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("button-account");
        submitButton.setOnAction(e -> {
            // Form validation
            if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                    dobPicker.getValue() == null || emailField.getText().isEmpty() ||
                    phoneField.getText().isEmpty() || addressField.getText().isEmpty() ||
                    salaryField.getText().isEmpty() || roleComboBox.getValue() == null ||
                    (!maleRadio.isSelected() && !femaleRadio.isSelected())) {
                showError("All fields must be filled correctly.");
                return;
            }

            // Ensure salary is a valid double
            double salary;
            try {
                salary = Double.parseDouble(salaryField.getText());
            } catch (NumberFormatException ex) {
                showError("Invalid salary value.");
                return;
            }

            employee.setFirstName(firstNameField.getText());
            employee.setLastName(lastNameField.getText());
            employee.setGender(maleRadio.isSelected());
            employee.setDob(java.sql.Date.valueOf(dobPicker.getValue()));
            employee.setEmail(emailField.getText());
            employee.setPhoneNumber(phoneField.getText());
            employee.setAddress(addressField.getText());
            employee.setSalary(salary);

            // Lấy ID tương ứng từ các ComboBox và gán null nếu không chọn
            int selectedStoreId = storeComboBox.getValue() != null ? storeComboBox.getValue().getKey() : 0;
            int selectedWarehouseId = warehouseComboBox.getValue() != null ? warehouseComboBox.getValue().getKey() : 0;

            employee.setIdStore(selectedStoreId );
            employee.setIdWarehouse(selectedWarehouseId);

            try {
                employeeModel.updateEmployee(employee);
                showSuccess("Employee updated successfully.");
                loadEmployees();
                tableView.refresh();
                dialog.close();
            } catch (Exception ex) {
                showError("Failed to update employee: " + ex.getMessage());
                loadEmployees();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-cancel-account");
        cancelButton.setOnAction(e -> dialog.close());

// Thêm các Label vào dialogLayout
        dialogLayout.add(firstNameLabel, 0, 0);
        dialogLayout.add(firstNameField, 1, 0);

        dialogLayout.add(lastNameLabel, 0, 1);
        dialogLayout.add(lastNameField, 1, 1);

        dialogLayout.add(genderLabel, 0, 2);
        dialogLayout.add(maleRadio, 1, 2);
        dialogLayout.add(femaleRadio, 1, 3);

        dialogLayout.add(dobLabel, 0, 4);
        dialogLayout.add(dobPicker, 1, 4);

        dialogLayout.add(emailLabel, 0, 5);
        dialogLayout.add(emailField, 1, 5);

        dialogLayout.add(phoneLabel, 0, 6);
        dialogLayout.add(phoneField, 1, 6);

        dialogLayout.add(addressLabel, 0, 7);
        dialogLayout.add(addressField, 1, 7);

        dialogLayout.add(salaryLabel, 0, 8);
        dialogLayout.add(salaryField, 1, 8);

        dialogLayout.add(roleLabel, 0, 9);
        dialogLayout.add(roleComboBox, 1, 9);

        dialogLayout.add(storeManagerLabel, 0, 10);
        dialogLayout.add(storeComboBox, 1, 10);

        dialogLayout.add(warehouseLabel, 0, 10);
        dialogLayout.add(warehouseComboBox, 1, 10);

        dialogLayout.add(submitButton, 0, 11);
        dialogLayout.add(cancelButton, 1, 11);

        Scene dialogScene = new Scene(dialogLayout, 500, 560);
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }


    private void deleteEmployee(Employee employee) {
        try {
            if (employeeModel.hasAccount(employee.getId())) {
                showError("Cannot delete employee with an account.");
                Stage stage = new Stage();
                DeletionFailed message = new DeletionFailed();
                message.start(stage);
            } else {
                employeeModel.deleteEmployee(employee.getId());
                employeeList.remove(employee);
                showSuccess("Employee deleted successfully.");
                Stage stage = new Stage();
                DeletionSuccess message = new DeletionSuccess();
                message.start(stage);
                loadEmployees();
            }
        } catch (SQLException e) {
            showError("Failed to delete employee: " + e.getMessage());
        }
    }

    private void loadRoles(ComboBox<Pair<Integer, String>> roleComboBox) {
        try {
            List<Pair<Integer, String>> roles = employeeModel.getAllRolesWithIds();
            roleComboBox.getItems().addAll(roles);

            // Tùy chỉnh cách hiển thị các vai trò
            roleComboBox.setCellFactory(lv -> new ListCell<Pair<Integer, String>>() {
                @Override
                protected void updateItem(Pair<Integer, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getValue()); // Chỉ hiển thị tên vai trò
                    }
                }
            });

            // Đảm bảo giá trị đã chọn cũng hiển thị đúng
            roleComboBox.setButtonCell(new ListCell<Pair<Integer, String>>() {
                @Override
                protected void updateItem(Pair<Integer, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getValue()); // Chỉ hiển thị tên vai trò
                    }
                }
            });

        } catch (SQLException e) {
            showError("Failed to load roles: " + e.getMessage());
        }
    }



    private void loadWarehouses(ComboBox<Pair<Integer, String>> warehouseComboBox) {
        try {
            List<Pair<Integer, String>> warehouses = employeeModel.getAllWarehousesWithIds(); // Cần phương thức này trong EmployeeModel
            warehouseComboBox.getItems().addAll(warehouses);

            // Tùy chỉnh cách hiển thị các kho
            warehouseComboBox.setCellFactory(lv -> new ListCell<Pair<Integer, String>>() {
                @Override
                protected void updateItem(Pair<Integer, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getValue()); // Chỉ hiển thị tên kho
                    }
                }
            });

            // Đảm bảo giá trị đã chọn cũng hiển thị đúng
            warehouseComboBox.setButtonCell(new ListCell<Pair<Integer, String>>() {
                @Override
                protected void updateItem(Pair<Integer, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getValue()); // Chỉ hiển thị tên kho
                    }
                }
            });

        } catch (SQLException e) {
            showError("Failed to load warehouses: " + e.getMessage());
        }
    }


    private void loadStores(ComboBox<Pair<Integer, String>> storeComboBox) {
        try {
            List<Pair<Integer, String>> stores = employeeModel.getAllStoresWithIds(); // Cần phương thức này trong EmployeeModel
            storeComboBox.getItems().addAll(stores);

            // Tùy chỉnh cách hiển thị các cửa hàng
            storeComboBox.setCellFactory(lv -> new ListCell<Pair<Integer, String>>() {
                @Override
                protected void updateItem(Pair<Integer, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select Stores");
                    } else {
                        setText(item.getValue()); // Hiển thị tên cửa hàng
                    }
                }
            });

            storeComboBox.setButtonCell(new ListCell<Pair<Integer, String>>() {
                @Override
                protected void updateItem(Pair<Integer, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select Stores");
                    } else {
                        setText(item.getValue()); // Hiển thị tên cửa hàng
                    }
                }
            });

        } catch (SQLException e) {
            showError("Failed to load stores: " + e.getMessage());
        }
    }



    public static void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

}