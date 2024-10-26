package view.buttondashboard;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import entity.Employee;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.EmployeeModel;

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

    public EmployeeManagementView() {
        employeeModel = new EmployeeModel();
        employeeList = FXCollections.observableArrayList();
        loadEmployees();

        tableView = new TableView<>();
        tableView.setItems(employeeList);
        configureTableView();

        searchField = new TextField();
        searchField.setPromptText("Search by Name");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchEmployees(newValue));

        Button addButton = new Button("Add Employee");
        addButton.setOnAction(e -> openAddEmployeeDialog());

        HBox searchBox = new HBox(10, searchField, addButton);
        searchBox.setPadding(new Insets(10));

        VBox vbox = new VBox(searchBox, tableView);
        vbox.setPadding(new Insets(10));


        this.getChildren().addAll(vbox);
    }


    private void configureTableView() {
        TableColumn<Employee, Number> sttCol = new TableColumn<>("No.");
        sttCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(cellData.getValue()) + 1));
        sttCol.setSortable(false);

        TableColumn<Employee, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(data -> data.getValue().firstNameProperty().concat(" ").concat(data.getValue().lastNameProperty()));

        TableColumn<Employee, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(data -> {
            boolean isMale = data.getValue().isGender();
            return new SimpleStringProperty(isMale ? "Male" : "Female");
        });
        TableColumn<Employee, LocalDate> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDob().toLocalDate()));

        TableColumn<Employee, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<Employee, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(data -> data.getValue().phoneNumberProperty());

        TableColumn<Employee, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> data.getValue().roleProperty());

        TableColumn<Employee, String> workplaceCol = new TableColumn<>("Workplace");
        workplaceCol.setCellValueFactory(data -> data.getValue().workplaceProperty());

        TableColumn<Employee, Double> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(data -> data.getValue().salaryProperty().asObject());

        TableColumn<Employee, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<Employee, String>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");


            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);  // Nếu hàng trống hoặc chỉ số không hợp lệ, không hiển thị gì
                } else {
                    Employee employee = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> openViewEmployeeDialog(employee));
                    editButton.setOnAction(e -> openEditEmployeeDialog(employee));
                    deleteButton.setOnAction(e -> deleteEmployee(employee));

                    HBox buttons = new HBox(viewButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }


        });

        tableView.getColumns().addAll(sttCol, fullNameCol, genderCol, roleCol, workplaceCol, actionCol);
    }

    private void loadEmployees() {
        try {
            employeeList.clear(); // Xóa danh sách cũ
            List<Employee> employees = employeeModel.getAllEmployees();
            employeeList.addAll(employees);
        } catch (Exception e) {
            showError("Failed to load employees: " + e.getMessage());
        }
    }

    private void searchEmployees(String name) {
        employeeList.clear();
        try {
            List<Employee> employees = employeeModel.searchEmployeesByName(name);
            employeeList.addAll(employees);
        } catch (SQLException e) {
            showError("Failed to search employees: " + e.getMessage());
        }
    }

    private void openAddEmployeeDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add Employee");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(10));

        // Khai báo các trường nhập liệu
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        Label firstNameError = new Label();
        firstNameError.setTextFill(Color.RED);
        firstNameError.setVisible(false);
        firstNameError.managedProperty().bind(firstNameError.visibleProperty());

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        Label lastNameError = new Label();
        lastNameError.setTextFill(Color.RED);
        lastNameError.setVisible(false);
        lastNameError.managedProperty().bind(lastNameError.visibleProperty());

        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        RadioButton femaleRadio = new RadioButton("Female");
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);

        TextField dobField = new TextField();
        dobField.setPromptText("DD/MM/YYYY");
        Label dobError = new Label();
        dobError.setTextFill(Color.RED);
        dobError.setVisible(false);
        dobError.managedProperty().bind(dobError.visibleProperty());

        dobField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 2 || newValue.length() == 5) {
                dobField.setText(newValue + "/");
                dobField.positionCaret(newValue.length() + 1);
            }
        });

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        Label emailError = new Label();
        emailError.setTextFill(Color.RED);
        emailError.setVisible(false);
        emailError.managedProperty().bind(emailError.visibleProperty());

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        Label phoneError = new Label();
        phoneError.setTextFill(Color.RED);
        phoneError.setVisible(false);
        phoneError.managedProperty().bind(phoneError.visibleProperty());

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        TextField salaryField = new TextField();
        salaryField.setPromptText("Salary");
        Label salaryError = new Label();
        salaryError.setTextFill(Color.RED);
        salaryError.setVisible(false);
        salaryError.managedProperty().bind(salaryError.visibleProperty());

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

        dobField.textProperty().addListener((observable, oldValue, newValue) -> {
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
        ComboBox<Pair<Integer, String>> storeComboBox = new ComboBox<>();
        ComboBox<Pair<Integer, String>> warehouseComboBox = new ComboBox<>();

        // Load roles từ cơ sở dữ liệu
        loadRoles(roleComboBox);

        // Thiết lập ComboBox store và warehouse không hiển thị ban đầu
        storeComboBox.setVisible(false);
        warehouseComboBox.setVisible(false);
        storeComboBox.managedProperty().bind(storeComboBox.visibleProperty());
        warehouseComboBox.managedProperty().bind(warehouseComboBox.visibleProperty());

        // Thay đổi hiển thị của ComboBox dựa trên vai trò đã chọn
        roleComboBox.setOnAction(event -> {
            String selectedRole = roleComboBox.getValue() != null ? roleComboBox.getValue().getValue() : null;

            if ("Store Management".equals(selectedRole) || "Cashier".equals(selectedRole)) {
                storeComboBox.setVisible(true);
                warehouseComboBox.setVisible(false);
                loadStores(storeComboBox); // Load stores
            } else if ("Warehouse Management".equals(selectedRole)) {
                storeComboBox.setVisible(false);
                warehouseComboBox.setVisible(true);
                loadWarehouses(warehouseComboBox); // Load warehouses
            }else {
                storeComboBox.setVisible(false);
                warehouseComboBox.setVisible(false);
            }
        });

        Button submitButton = new Button("Submit");
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
            } else if (!firstName.matches("[a-zA-Z]+")) {
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
        cancelButton.setOnAction(e -> dialog.close());

        dialogLayout.getChildren().addAll(firstNameField, firstNameError, lastNameField, lastNameError, maleRadio, femaleRadio,
                dobField, dobError, emailField, emailError, phoneField, phoneError, addressField, salaryField, salaryError,
                roleComboBox, storeComboBox, warehouseComboBox, submitButton, cancelButton);

        Scene dialogScene = new Scene(dialogLayout, 400, 500);
        dialog.setScene(dialogScene);
        dialog.show();
    }


    private void openViewEmployeeDialog(Employee employee) {
        Stage dialog = new Stage();
        dialog.setTitle("View Employee");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(10));

        Label firstNameLabel = new Label("First Name: " + employee.getFirstName());
        Label lastNameLabel = new Label("Last Name: " + employee.getLastName());
        Label genderLabel = new Label("Gender: " + (employee.isGender() ? "Male" : "Female"));
        Label dobLabel = new Label("Date of Birth: " + employee.getDob());
        Label emailLabel = new Label("Email: " + employee.getEmail());
        Label phoneLabel = new Label("Phone Number: " + employee.getPhoneNumber());
        Label addressLabel = new Label("Address: " + employee.getAddress());
        Label salaryLabel = new Label("Salary: " + employee.getSalary());
        Label roleLabel = new Label("Role: " + employee.getRole());
        Label workplaceLabel = new Label("Workplace: " + employee.getWorkplace());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());

        dialogLayout.getChildren().addAll(firstNameLabel, lastNameLabel, genderLabel, dobLabel, emailLabel,
                phoneLabel, addressLabel, salaryLabel, roleLabel, workplaceLabel, closeButton);

        Scene dialogScene = new Scene(dialogLayout, 300, 400);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void openEditEmployeeDialog(Employee employee) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Employee");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogLayout = new VBox(10);
        dialogLayout.setPadding(new Insets(10));

        TextField firstNameField = new TextField(employee.getFirstName());
        TextField lastNameField = new TextField(employee.getLastName());

        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        RadioButton femaleRadio = new RadioButton("Female");
        if (employee.isGender()) {
            maleRadio.setSelected(true);
        } else {
            femaleRadio.setSelected(true);
        }
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);

        DatePicker dobPicker = new DatePicker(employee.getDob().toLocalDate());
        TextField emailField = new TextField(employee.getEmail());
        TextField phoneField = new TextField(employee.getPhoneNumber());
        TextField addressField = new TextField(employee.getAddress());
        TextField salaryField = new TextField(String.valueOf(employee.getSalary()));

        ComboBox<Pair<Integer, String>> roleComboBox = new ComboBox<>();
        ComboBox<Pair<Integer, String>> storeComboBox = new ComboBox<>();
        ComboBox<Pair<Integer, String>> warehouseComboBox = new ComboBox<>();

        // Load roles from the database
        loadRoles(roleComboBox);

        // Set selected role
        roleComboBox.setValue(new Pair<>(employee.getIdRole(), employee.getRole()));

        // Thay đổi ComboBox hiển thị theo vai trò
        roleComboBox.setOnAction(event -> {
            String selectedRole = roleComboBox.getValue() != null ? roleComboBox.getValue().getValue() : null;
            if ("Store Management".equals(selectedRole)) {
                storeComboBox.setVisible(true);
                warehouseComboBox.setVisible(false);
                loadStores(storeComboBox); // Load stores
                storeComboBox.setValue(new Pair<>(employee.getIdStore(), employeeModel.getStoreNameById(employee.getIdStore())));
            } else if ("Warehouse Management".equals(selectedRole)) {
                storeComboBox.setVisible(false);
                warehouseComboBox.setVisible(true);
                loadWarehouses(warehouseComboBox); // Load warehouses
                warehouseComboBox.setValue(new Pair<>(employee.getIdWarehouse(), employeeModel.getWarehouseNameById(employee.getIdWarehouse())));
            } else {
                storeComboBox.setVisible(false);
                warehouseComboBox.setVisible(false);
            }
        });

        // Kích hoạt lần đầu
        roleComboBox.fireEvent(new ActionEvent());

        Button submitButton = new Button("Submit");
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

            employee.setIdStore(selectedStoreId);
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
        cancelButton.setOnAction(e -> dialog.close());

        dialogLayout.getChildren().addAll(firstNameField, lastNameField, maleRadio, femaleRadio, dobPicker,
                emailField, phoneField, addressField, salaryField, roleComboBox, storeComboBox, warehouseComboBox, submitButton, cancelButton);

        Scene dialogScene = new Scene(dialogLayout, 400, 500);
        dialog.setScene(dialogScene);
        dialog.show();
    }


    private void deleteEmployee(Employee employee) {
        try {
            if (employeeModel.hasAccount(employee.getId())) {
                showError("Cannot delete employee with an account.");
            } else {
                employeeModel.deleteEmployee(employee.getId());
                employeeList.remove(employee);
                showSuccess("Employee deleted successfully.");
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
                        setText(null);
                    } else {
                        setText(item.getValue()); // Chỉ hiển thị tên cửa hàng
                    }
                }
            });

            // Đảm bảo giá trị đã chọn cũng hiển thị đúng
            storeComboBox.setButtonCell(new ListCell<Pair<Integer, String>>() {
                @Override
                protected void updateItem(Pair<Integer, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getValue()); // Chỉ hiển thị tên cửa hàng
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