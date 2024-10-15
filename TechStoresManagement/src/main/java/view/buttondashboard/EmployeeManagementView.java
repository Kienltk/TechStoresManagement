package view.buttondashboard;

import entity.Employee;
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
            addEmployeeStage.setTitle("Thêm nhân viên");

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

            addEmployeeForm.add(new Label("Date of Bỉrth:"), 0, 5);
            DatePicker dobDatePicker = new DatePicker();
            addEmployeeForm.add(dobDatePicker, 1, 5);

            addEmployeeForm.add(new Label("Salary:"), 0, 6);
            TextField salaryField = new TextField();
            addEmployeeForm.add(salaryField, 1, 6);

            addEmployeeForm.add(new Label("Role:"), 0, 7);
            ObservableList<String> roles = FXCollections.observableArrayList(employeeModel.getAllRoles());
            ComboBox<String> roleComboBox = new ComboBox<>(roles);
            addEmployeeForm.add(roleComboBox, 1, 7);

            addEmployeeForm.add(new Label("Work Place:"), 0, 8);
            ObservableList<List<String>> workplaces = FXCollections.observableArrayList(employeeModel.getAllStores(), employeeModel.getAllWarehouses());
            ComboBox<List<String>> workplaceComboBox = new ComboBox<>(workplaces);
            addEmployeeForm.add(workplaceComboBox, 1, 8);

            addEmployeeForm.add(new Label("Status:"), 0, 9);
            TextField statusField = new TextField();
            addEmployeeForm.add(statusField, 1, 9);

            // Add a save button to save the new employee
            Button saveButton = new Button("Save");
            saveButton.setOnAction(event -> {
                // Create a new employee
                Employee newEmployee = new Employee();
                newEmployee.setFirstName(firstNameField.getText());
                newEmployee.setLastName(lastNameField.getText());
                newEmployee.setEmail(emailField.getText());
                newEmployee.setPhoneNumber(phoneNumberField.getText());
                newEmployee.setAddress(addressField.getText());
                newEmployee.setDob(Date.valueOf(dobDatePicker.getValue()));
                newEmployee.setSalary(Double.parseDouble(salaryField.getText()));
                newEmployee.setRole(roleComboBox.getSelectionModel().getSelectedItem());
                newEmployee.setWorkplace(String.valueOf(workplaceComboBox.getSelectionModel().getSelectedItem()));
                newEmployee.setStatus(statusField.getText());

                // Add the new employee to the database
                int roleId = employeeModel.getRoleIdFromName(newEmployee.getRole());
                int workplaceId = workplaceComboBox.getSelectionModel().getSelectedItem().toString().contains("Store") ?
                        employeeModel.getIdStoreFromName(workplaceComboBox.getSelectionModel().getSelectedItem().toString().replaceAll("Store", "")) :
                        employeeModel.getIdWarehouseFromName(workplaceComboBox.getSelectionModel().getSelectedItem().toString().replaceAll("Warehouse", ""));
                employeeModel.addEmployee(newEmployee, roleId, workplaceId);

                // Close the add employee stage
                addEmployeeStage.close();

                // Update the employee table
                employeeTable.setItems(FXCollections.observableArrayList(employeeModel.getAllEmployees()));
            });
            addEmployeeForm.add(saveButton, 1, 10);

            // Create a scene for the add employee stage
            Scene addEmployeeScene = new Scene(addEmployeeForm, 600, 400);
            addEmployeeStage.setScene(addEmployeeScene);

            // Show the add employee stage
            addEmployeeStage.showAndWait();
        });

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search Item");
        Button searchButton = new Button("🔍");
        HBox searchBar = new HBox(searchField, searchButton);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setSpacing(10);

        AtomicLong lastSearchTime = new AtomicLong();
        AtomicReference<ObservableList<Employee>> lastSearchResult = new AtomicReference<>();

        searchField .textProperty().addListener((observable, oldValue, newValue) -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSearchTime.get() > 500) { // trì hoãn tìm kiếm 500ms
                lastSearchTime.set(currentTime);
                String searchQuery = newValue.toLowerCase();
                ObservableList<Employee> filteredEmployees = FXCollections.observableArrayList();

                List<Employee> allEmployees = employeeModel.getAllEmployees();

                for (Employee employee : allEmployees) {
                    if (employee.getFirstName().toLowerCase().contains(searchQuery) || employee.getLastName().toLowerCase().contains(searchQuery)) {
                        filteredEmployees.add(employee);
                    }
                }

                lastSearchResult.set(filteredEmployees);
                employeeTable.setItems(filteredEmployees);
            }
        });
        // Table Columns
        TableColumn<Employee, String> idColumn = new TableColumn<>("ID");
        idColumn.setMinWidth(30);
        idColumn.setPrefWidth(40);
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        TableColumn<Employee, String> fullNameColumn = new TableColumn<>("Full Name");
        fullNameColumn.setMinWidth(70);
        fullNameColumn.setPrefWidth(100);
        fullNameColumn.setCellValueFactory(cellData -> {
            Employee employee = cellData.getValue();
            return new SimpleStringProperty(employee.getFirstName() + " " + employee.getLastName());
        });

        TableColumn<Employee, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setMinWidth(70);
        roleColumn.setPrefWidth(140);
        roleColumn.setCellValueFactory(cellData -> {
            int roleId = cellData.getValue().getIdRole();
            String roleName = employeeModel.getRoleName(roleId);
            return new SimpleStringProperty(roleName);
        });

        TableColumn<Employee, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setMinWidth(100);
        emailColumn.setPrefWidth(220);
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        TableColumn<Employee, String> phoneNumberColumn = new TableColumn<>("Phone Number");
        phoneNumberColumn.setMinWidth(80);
        phoneNumberColumn.setPrefWidth(120);
        phoneNumberColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());

        TableColumn<Employee, String> workplaceColumn = new TableColumn<>("Workplace");
        workplaceColumn.setMinWidth(70);
        workplaceColumn.setPrefWidth(140);
        workplaceColumn.setCellValueFactory(cellData -> cellData.getValue().workplaceProperty());

        TableColumn<Employee, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setMinWidth(50);
        statusColumn.setPrefWidth(75);
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Option Column with Edit and Delete buttons
        TableColumn<Employee, Void> optionColumn = new TableColumn<>("Option");
        optionColumn.setCellFactory(col -> new TableCell<>() {
            final Button editButton = new Button("Edit");
            final Button deleteButton = new Button("Delete");
            final Button viewButton = new Button("View");
            {
                editButton.setStyle("-fx-background-color: yellow;");
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                viewButton.setStyle("-fx-background-color: #00C2FF; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox optionBox = new HBox(editButton, deleteButton, viewButton);
                    optionBox.setSpacing(10);
                    setGraphic(optionBox);

                    // Add action for Edit button
                    editButton.setOnAction(e -> {
                        Employee employee = getTableView().getItems().get(getIndex());
                        // Open a new window to edit employee
                        Stage editStage = new Stage();
                        editStage.initModality(Modality.APPLICATION_MODAL);
                        editStage.setTitle("Edit Employee");

                        // Create a form to edit employee
                        GridPane editForm = new GridPane();
                        editForm.setHgap(10);
                        editForm.setVgap(10);

                        // Add fields to form
                        editForm.add(new Label("First Name:"), 0, 0);
                        TextField firstNameField = new TextField(employee.getFirstName());
                        editForm.add(firstNameField, 1, 0);

                        editForm.add(new Label("Last Name:"), 0, 1);
                        TextField lastNameField = new TextField(employee.getLastName());
                        editForm.add(lastNameField, 1, 1);

                        editForm.add(new Label("Email:"), 0, 2);
                        TextField emailField = new TextField(employee.getEmail ());
                        editForm.add(emailField, 1, 2);

                        editForm.add(new Label("Phone Number:"), 0, 3);
                        TextField phoneNumberField = new TextField(employee.getPhoneNumber());
                        editForm.add(phoneNumberField, 1, 3);

                        editForm.add(new Label("Workplace:"), 0, 4);
                        ComboBox<String> workplaceComboBox = new ComboBox<>();
                        List<String> warehouses = employeeModel.getAllWarehouses();
                        List<String> stores = employeeModel.getAllStores();

                        for (String warehouse : warehouses) {
                            workplaceComboBox.getItems().add("Warehouse " + warehouse);
                        }
                        for (String store : stores) {
                            workplaceComboBox.getItems().add("Store " + store);
                        }

                        if (employee.getIdStore() != null) {
                            workplaceComboBox.getSelectionModel().select("Store " + employee.getWorkplace());
                        } else if (employee.getIdWarehouse() != null) {
                            workplaceComboBox.getSelectionModel().select("Warehouse " + employee.getWorkplace());
                        }

                        workplaceComboBox.setOnAction(e1 -> {
                            String selectedWorkplace = workplaceComboBox.getSelectionModel().getSelectedItem();
                            employee.setWorkplace(selectedWorkplace.replace("Warehouse ", "").replace("Store ", ""));
                        });
                        ObservableList<String> workplaceList = FXCollections.observableArrayList();
                        workplaceList.addAll(workplaceComboBox.getItems());
                        workplaceComboBox.setItems(workplaceList);
                        editForm.add(workplaceComboBox, 1, 4);

                        editForm.add(new Label("Gender:"), 0, 5);
                        ComboBox<String> genderComboBox = new ComboBox<>();
                        genderComboBox.getItems().addAll("Male", "Female");
                        genderComboBox.getSelectionModel().select(employee.isGender() ? "Male" : "Female");
                        editForm.add(genderComboBox, 1, 5);

                        editForm.add(new Label("Date of Birth:"), 0, 6);
                        DatePicker dobDatePicker = new DatePicker();
                        dobDatePicker.setValue(employee.getDob().toLocalDate());
                        editForm.add(dobDatePicker, 1, 6);

                        editForm.add(new Label("Address:"), 0, 7);
                        TextField addressField = new TextField(employee.getAddress());
                        editForm.add(addressField, 1, 7);

                        editForm.add(new Label("Hire Date:"), 0, 8);
                        DatePicker hireDateDatePicker = new DatePicker();
                        hireDateDatePicker.setValue(employee.getHireDate().toLocalDate());
                        editForm.add(hireDateDatePicker, 1, 8);

                        editForm.add(new Label("Salary:"), 0, 9);
                        TextField salaryField = new TextField(String.valueOf(employee.getSalary()));
                        editForm.add(salaryField, 1, 9);

                        editForm.add(new Label("Role:"), 0, 10);
                        TextField roleField = new TextField(employee.getRole());
                        editForm.add(roleField, 1, 10);

                        editForm.add(new Label("Status:"), 0, 11);
                        TextField statusField = new TextField(employee.getStatus());
                        editForm.add(statusField, 1, 11);

                        // Add Save button to form
                        Button saveButton = new Button("Save");
                        saveButton.setOnAction(event -> {
                            // Update employee information
                            employee.setFirstName(firstNameField.getText());
                            employee.setLastName(lastNameField.getText());
                            employee.setEmail(emailField.getText());
                            employee.setPhoneNumber(phoneNumberField.getText());
                            employee.setWorkplace(workplaceComboBox.getSelectionModel().getSelectedItem());
                            employee.setGender(genderComboBox.getSelectionModel().getSelectedItem().equals("Male"));
                            employee.setDob(Date.valueOf(dobDatePicker.getValue()));
                            employee.setAddress(addressField.getText());
                            employee.setHireDate(Date.valueOf(hireDateDatePicker.getValue()));
                            employee.setSalary(Double.parseDouble(salaryField.getText()));
                            employee.setRole(roleField.getText());
                            employee.setStatus(statusField.getText());

                            // Update employee information in database
                            employeeModel.updateEmployee(employee);

                            // Close the edit window
                            editStage.close();
                        });
                        editForm.add(saveButton, 1, 12);

                        // Set the form into the edit window
                        Scene editScene = new Scene(editForm, 600, 500);
                        editStage.setScene(editScene);

                        // Show the edit window
                        editStage.showAndWait();
                    });

                    // Add action for Delete button
                    deleteButton.setOnAction(e -> {
                        Employee employee = getTableView().getItems().get(getIndex());
                        employeeModel.deleteEmployee(employee.getId());
                        employeeTable.setItems(FXCollections.observableArrayList(employeeModel.getAllEmployees()));
                    });

                    viewButton.setOnAction(e -> {
                        Employee employee = getTableView().getItems().get(getIndex());
                        showEmployeeDetails(employee);
                    });
                }
            }
        });

        // Add Columns to Table
        employeeTable.getColumns().addAll(idColumn,fullNameColumn,  emailColumn, phoneNumberColumn,roleColumn, workplaceColumn, statusColumn, optionColumn);

        // Set the table's width to fit all columns
        employeeTable.setPrefWidth(1000); // adjust this value to fit your needs

        // Wrap the table in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(employeeTable);
        scrollPane.setFitToWidth(true);

        // Add components to VBox
        this.getChildren().addAll(titleLabel, addEmployeeButton, searchBar, scrollPane);
    }

    private void showEmployeeDetails(Employee employee) {
        // Create a new popup window
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Employee Details");

        // Create a VBox to hold the employee details
        VBox popupContent = new VBox(10);

        // Create a GridPane to hold the employee details
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Add employee details to the GridPane
        gridPane.add(new Label("ID:"), 0, 0);
        gridPane.add(new Label(String.valueOf(employee.getId())), 1, 0);

        gridPane.add(new Label("First Name:"), 0, 1);
        gridPane.add(new Label(employee.getFirstName()), 1, 1);

        gridPane.add(new Label("Last Name:"), 0, 2);
        gridPane.add(new Label(employee.getLastName()), 1, 2);

        gridPane.add(new Label("Gender:"), 0, 3);
        gridPane.add(new Label(employee.isGender() ? "Male" : "Female"), 1, 3);

        gridPane.add(new Label("Date of Birth:"), 0, 4);
        gridPane.add(new Label(employee.getDob().toString()), 1, 4);

        gridPane.add(new Label("Email:"), 0, 5);
        gridPane.add(new Label(employee.getEmail()), 1, 5);

        gridPane.add(new Label("Phone Number:"), 0, 6);
        gridPane.add(new Label(employee.getPhoneNumber()), 1, 6);

        gridPane.add(new Label("Address:"), 0, 7);
        gridPane.add(new Label(employee.getAddress()), 1, 7);

        gridPane.add(new Label("Hire Date:"), 0, 8);
        gridPane.add(new Label(employee.getHireDate().toString()), 1, 8);

        gridPane.add(new Label("Salary:"), 0, 9);
        gridPane.add(new Label(String.valueOf(employee.getSalary())), 1, 9);

        gridPane.add(new Label("Role:"), 0, 10);
        gridPane.add(new Label(employee.getRole()), 1, 10);

        gridPane.add(new Label("Workplace:"), 0, 11);
        gridPane.add(new Label(employee.getWorkplace()), 1, 11);

        gridPane.add(new Label("Status:"), 0, 12);
        gridPane.add(new Label(employee.getStatus()), 1, 12);

        // Add the GridPane to the VBox
        popupContent.getChildren().add(gridPane);

        // Create a HBox to hold the action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);

        // Create the action buttons
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            // Code to edit employee
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            // Code to delete employee
        });

        // Add the action buttons to the HBox
        actionBox.getChildren().addAll(editButton, deleteButton);

        // Add the HBox to the VBox
        popupContent.getChildren().add(actionBox);

        // Set the popup content
        Scene popupScene = new Scene(popupContent, 600, 400);
        popupStage.setScene(popupScene);

        // Show the popup
        popupStage.showAndWait();
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