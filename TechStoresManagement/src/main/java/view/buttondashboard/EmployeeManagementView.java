package view.buttondashboard;

import entity.Employee;
import entity.Role;
import entity.Workplace;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.EmployeeModel;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class EmployeeManagementView extends VBox {

    private EmployeeModel employeeModel;
    private TableView<Employee> employeeTable;
    private ObservableList<Employee> employeeList;

    public EmployeeManagementView() {
        employeeModel = new EmployeeModel();
        employeeList = FXCollections.observableArrayList(employeeModel.getAllEmployees());

        // Title Label
        Label titleLabel = new Label("Employee Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // TableView for Employee Data
        employeeTable = new TableView<>();
        employeeTable.setPrefWidth(1300);
        employeeTable.setStyle("-fx-background-color: #FFFFFF");
        employeeTable.setFixedCellSize(52);
        employeeTable.setPrefHeight(600);
        employeeTable.setMaxHeight(600);
        employeeTable.setItems(employeeList);

        // New Employee Button
        Button addEmployeeButton = new Button("New Employee");
        addEmployeeButton.setStyle("-fx-background-color: #00C2FF; -fx-text-fill: white;");
        addEmployeeButton.setOnAction(e -> {
            // Create a new stage for adding a new employee
            Stage addEmployeeStage = new Stage();
            addEmployeeStage.initModality(Modality.APPLICATION_MODAL);
            addEmployeeStage.setTitle("Add Employee");

            // Create a GridPane for adding a new employee
            GridPane addEmployeeForm = new GridPane();
            addEmployeeForm.setHgap(10);
            addEmployeeForm.setVgap(10);

            // Add fields for adding a new employee
            addEmployeeForm.add(new Label("First Name:"), 0, 0);
            TextField firstNameField = new TextField();
            addEmployeeForm.add(firstNameField, 1, 0);

            addEmployeeForm.add(new Label("Last Name:"), 0, 1);
            TextField lastNameField = new TextField();
            addEmployeeForm.add(lastNameField, 1, 1);

            addEmployeeForm.add(new Label("Email:"), 0, 2);
            TextField emailField = new TextField();
            addEmployeeForm.add(emailField, 1, 2);

            addEmployeeForm.add(new Label("Phone:"), 0, 3);
            TextField phoneNumberField = new TextField();
            addEmployeeForm.add(phoneNumberField, 1, 3);

            addEmployeeForm.add(new Label("Address:"), 0, 4);
            TextField addressField = new TextField();
            addEmployeeForm.add(addressField, 1, 4);

            addEmployeeForm.add(new Label("Date of Birth:"), 0, 5);
            DatePicker dobDatePicker = new DatePicker();
            addEmployeeForm.add(dobDatePicker, 1, 5);

            addEmployeeForm.add(new Label("Salary:"), 0, 6);
            TextField salaryField = new TextField();
            addEmployeeForm.add(salaryField, 1, 6);

            addEmployeeForm.add(new Label("Role:"), 0, 7);
            ComboBox<Role> roleComboBox = new ComboBox<>();
            roleComboBox.setItems(FXCollections.observableArrayList(employeeModel.getAllRoles()));
            roleComboBox.setOnAction(event -> {
                Role selectedRole = roleComboBox.getSelectionModel().getSelectedItem();
                if (selectedRole != null) {
                    int roleId = selectedRole.getId();
                    System.out.println("Selected Role ID: " + roleId);
                }
            });
            addEmployeeForm.add(roleComboBox, 1, 7);

            addEmployeeForm.add(new Label("Work Place:"), 0, 8);

            ComboBox<Workplace> workplaceComboBox = new ComboBox<>();
            workplaceComboBox.setItems(employeeModel.getAllWorkplaces());
            workplaceComboBox.setOnAction(event -> {
                Workplace selectedWorkplace = workplaceComboBox.getSelectionModel().getSelectedItem();
                if (selectedWorkplace != null) {
                    int workplaceId = selectedWorkplace.getId();
                    boolean isStore = selectedWorkplace.isStore();
                    if (isStore) {
                        // Lấy idStore và set idWarehouse null
                        int idStore = workplaceId;
                        int idWarehouse = Integer.parseInt(null);
                    } else {
                        // Lấy idWarehouse và set idStore null
                        int idStore = Integer.parseInt(null);
                        int idWarehouse = workplaceId;
                    }
                }
            });
            addEmployeeForm.add(workplaceComboBox, 1, 8);
            addEmployeeForm.add(new Label("Status:"), 0, 9);
            TextField statusField = new TextField();
            addEmployeeForm.add(statusField, 1, 9);

            // Add Save button to form
            Button saveButton = new Button("Save");
            saveButton.setOnAction(event -> {
                // Create a new employee
                Employee employee = new Employee();
                employee.setFirstName(firstNameField.getText());
                employee.setLastName(lastNameField.getText());
                employee.setEmail(emailField.getText());
                employee.setPhoneNumber(phoneNumberField.getText());
                employee.setAddress(addressField.getText());
                employee.setDob(Date.valueOf(dobDatePicker.getValue()));
                employee.setSalary(Double.parseDouble(salaryField.getText()));
                Role selectedRole = roleComboBox.getSelectionModel().getSelectedItem();
                if (selectedRole != null) {
                    employee.setIdRole(selectedRole.getId());
                }
                Workplace selectedWorkplace = workplaceComboBox.getSelectionModel().getSelectedItem();
                if (selectedWorkplace != null) {
                    if (selectedWorkplace.isStore()) {
                        employee.setIdStore(selectedWorkplace.getId());
                    } else {
                        employee.setIdWarehouse(selectedWorkplace.getId());
                    }
                }
                employee.setStatus(statusField.getText());

                // Add employee to database
                employeeModel.addEmployee(employee, employee.getIdRole(), employee.getIdStore(), employee.getIdWarehouse());

                // Add employee to table
                employeeList.add(employee);

                // Close the add employee window
                addEmployeeStage.close();
            });
            addEmployeeForm.add(saveButton, 1, 10);

            // Set the form into the add employee window
            Scene addEmployeeScene = new Scene(addEmployeeForm, 600, 500);
            addEmployeeStage.setScene(addEmployeeScene);

            // Show the add employee window
            addEmployeeStage.showAndWait();
        });

        // Search Bar
        TextField searchBar = new TextField();
        searchBar.setPromptText("Search Employee");

        // Add columns to table
        TableColumn<Employee, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));

        TableColumn<Employee, String> fullNameColumn = new TableColumn<>("Full Name");
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));

        TableColumn<Employee, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));

        TableColumn<Employee, String> phoneNumberColumn = new TableColumn<>("Phone Number");
        phoneNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhoneNumber()));

        TableColumn<Employee, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));

        TableColumn<Employee, String> workplaceColumn = new TableColumn<>("Workplace");
        workplaceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getWorkplace()));

        TableColumn<Employee, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        TableColumn<Employee, String> optionColumn = new TableColumn<>("Option");
        optionColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Edit | Delete"));

        // Add columns to table
        employeeTable.getColumns().addAll(idColumn, fullNameColumn, emailColumn, phoneNumberColumn, roleColumn, workplaceColumn, statusColumn, optionColumn);

        // Set the table's width to fit all columns
        employeeTable.setPrefWidth(1300); // adjust this value to fit your needs

        // Wrap the table in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(employeeTable);
        scrollPane.setFitToWidth(true);

        // Add components to VBox
        this.getChildren().addAll(titleLabel, addEmployeeButton, searchBar, scrollPane);
    }

    public static void main(String[] args) {
        EmployeeManagementView view = new EmployeeManagementView();
        Scene scene = new Scene(view, 1300, 1000);
        Stage primaryStage = new Stage();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Employee Management");
        primaryStage.show();
    }
}