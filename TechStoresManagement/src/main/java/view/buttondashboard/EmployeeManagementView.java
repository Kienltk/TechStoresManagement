package view.buttondashboard;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import entity.Employee;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.EmployeeModel;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
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
        TableColumn<Employee, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(data -> data.getValue().firstNameProperty().concat(" ").concat(data.getValue().lastNameProperty()));

        TableColumn<Employee, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(data -> data.getValue().genderProperty().asString());

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

        tableView.getColumns().addAll(fullNameCol, genderCol, dobCol, emailCol, phoneCol, roleCol, workplaceCol, salaryCol, actionCol);
    }

    private void loadEmployees() {
        try {
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

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        RadioButton femaleRadio = new RadioButton("Female");
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);

        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Date of Birth");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        TextField salaryField = new TextField();
        salaryField.setPromptText("Salary");

        ComboBox<String> roleComboBox = new ComboBox<>();
        ComboBox<String> workplaceComboBox = new ComboBox<>();

        // Load roles and workplaces from the database
        loadRoles(roleComboBox);
        loadWorkplaces(workplaceComboBox);

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            // Form validation
            if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                    dobPicker.getValue() == null || emailField.getText().isEmpty() ||
                    phoneField.getText().isEmpty() || addressField.getText().isEmpty() ||
                    salaryField.getText().isEmpty() || roleComboBox.getValue() == null ||
                    workplaceComboBox.getValue() == null || (!maleRadio.isSelected() && !femaleRadio.isSelected())) {
                // Show an alert or return early if any field is empty or invalid
                System.out.println("All fields must be filled correctly");
                return;
            }

            // Ensure salary is a valid double
            double salary;
            try {
                salary = Double.parseDouble(salaryField.getText());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid salary value");
                return;
            }

            Employee newEmployee = new Employee();
            newEmployee.setFirstName(firstNameField.getText());
            newEmployee.setLastName(lastNameField.getText());
            newEmployee.setGender(maleRadio.isSelected());
            newEmployee.setDob(java.sql.Date.valueOf(dobPicker.getValue()));
            newEmployee.setEmail(emailField.getText());
            newEmployee.setPhoneNumber(phoneField.getText());
            newEmployee.setAddress(addressField.getText());
            newEmployee.setSalary(Double.parseDouble(salaryField.getText()));
            newEmployee.setRole(roleComboBox.getValue());
            newEmployee.setWorkplace(workplaceComboBox.getValue());

            try {
                employeeModel.insertEmployee(newEmployee);
                employeeList.add(newEmployee);
                showSuccess("Employee added successfully.");
                dialog.close();
            } catch (Exception ex) {
                showError("Failed to add employee: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());

        dialogLayout.getChildren().addAll(firstNameField, lastNameField, maleRadio, femaleRadio, dobPicker,
                emailField, phoneField, addressField, salaryField, roleComboBox, workplaceComboBox, submitButton, cancelButton);

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

        ComboBox<String> roleComboBox = new ComboBox<>();
        ComboBox<String> workplaceComboBox = new ComboBox<>();

        // Load roles and workplaces from the database
        loadRoles(roleComboBox);
        loadWorkplaces(workplaceComboBox);

        // Set selected role and workplace
        roleComboBox.setValue(employee.getRole());
        workplaceComboBox.setValue(employee.getWorkplace());

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            // Form validation
            if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                    dobPicker.getValue() == null || emailField.getText().isEmpty() ||
                    phoneField.getText().isEmpty() || addressField.getText().isEmpty() ||
                    salaryField.getText().isEmpty() || roleComboBox.getValue() == null ||
                    workplaceComboBox.getValue() == null || (!maleRadio.isSelected() && !femaleRadio.isSelected())) {
                // Show an alert or return early if any field is empty or invalid
                System.out.println("All fields must be filled correctly");
                return;
            }

            // Ensure salary is a valid double
            double salary;
            try {
                salary = Double.parseDouble(salaryField.getText());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid salary value");
                return;
            }

            employee.setFirstName(firstNameField.getText());
            employee.setLastName(lastNameField.getText());
            employee.setGender(maleRadio.isSelected());
            employee.setDob(java.sql.Date.valueOf(dobPicker.getValue()));
            employee.setEmail(emailField.getText());
            employee.setPhoneNumber(phoneField.getText());
            employee.setAddress(addressField.getText());
            employee.setSalary(Double.parseDouble(salaryField.getText()));
            employee.setRole(roleComboBox.getValue());
            employee.setWorkplace(workplaceComboBox.getValue());

            try {
                employeeModel.updateEmployee(employee);
                tableView.refresh();
                showSuccess("Employee updated successfully.");
                dialog.close();
            } catch (Exception ex) {
                showError("Failed to update employee: " + ex.getMessage());
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());

        dialogLayout.getChildren().addAll(firstNameField, lastNameField, maleRadio, femaleRadio, dobPicker,
                emailField, phoneField, addressField, salaryField, roleComboBox, workplaceComboBox, submitButton, cancelButton);

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
            }
        } catch (SQLException e) {
            showError("Failed to delete employee: " + e.getMessage());
        }
    }

    private void loadRoles(ComboBox<String> roleComboBox) {
        try {
            List<String> roles = employeeModel.getAllRoles();
            roleComboBox.getItems().addAll(roles);
        } catch (SQLException e) {
            showError("Failed to load roles: " + e.getMessage());
        }
    }

    private void loadWorkplaces(ComboBox<String> workplaceComboBox) {
        try {
            List<String> workplaces = employeeModel.getAllWorkplaces();
            workplaceComboBox.getItems().addAll(workplaces);
        } catch (SQLException e) {
            showError("Failed to load workplaces: " + e.getMessage());
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