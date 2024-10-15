package entity;

import javafx.beans.property.*;
import java.sql.Date;

public class Employee {
    private  SimpleIntegerProperty id = new SimpleIntegerProperty(this, "id", 0);
    private final SimpleStringProperty firstName = new SimpleStringProperty(this, "firstName", "");
    private final SimpleStringProperty lastName = new SimpleStringProperty(this, "lastName", "");
    private final SimpleBooleanProperty gender = new SimpleBooleanProperty(this, "gender", false);
    private final SimpleObjectProperty<Date> dob = new SimpleObjectProperty<>(this, "dob", null);
    private final SimpleStringProperty email = new SimpleStringProperty(this, "email", "");
    private final SimpleStringProperty phoneNumber = new SimpleStringProperty(this, "phoneNumber", "");
    private final SimpleStringProperty address = new SimpleStringProperty(this, "address", "");
    private final SimpleObjectProperty<Date> hireDate = new SimpleObjectProperty<>(this, "hireDate", null);
    private final SimpleDoubleProperty salary = new SimpleDoubleProperty(this, "salary", 0.0);
    private final SimpleIntegerProperty idRole = new SimpleIntegerProperty(this, "idRole", 0);
    private final SimpleIntegerProperty idStore = new SimpleIntegerProperty(this, "idStore", 0);
    private final SimpleIntegerProperty idWarehouse = new SimpleIntegerProperty(this, "idWarehouse", 0);
    private final SimpleStringProperty status = new SimpleStringProperty(this, "status", "");
    private final SimpleStringProperty role = new SimpleStringProperty(this, "role", "");
    private final SimpleStringProperty workplace = new SimpleStringProperty(this, "workplace", "");

    // No-argument constructor
    public Employee() {
        // Default values can be initialized here if needed
    }

    // Parameterized constructor
    public Employee(int id, String firstName, String lastName, boolean gender, Date dob, String email,
                    String phoneNumber, String address, Date hireDate, double salary,
                    int idRole, Integer idStore, Integer idWarehouse, String status, String role, String workplace) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setGender(gender);
        setDob(dob);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
        setHireDate(hireDate);
        setSalary(salary);
        setIdRole(idRole);

        // Kiểm tra xem idStore có phải là null hay không
        if (idStore != null) {
            setIdStore(idStore);
        } else {
            setIdStore(0); // Thiết lập giá trị mặc định cho idStore
        }

        // Kiểm tra xem idWarehouse có phải là null hay không
        if (idWarehouse != null) {
            setIdWarehouse(idWarehouse);
        } else {
            setIdWarehouse(0); // Thiết lập giá trị mặc định cho idWarehouse
        }

        setStatus(status);
        setRole(role);
        setWorkplace(workplace);
    }

    public Employee( String firstName, String lastName, boolean gender, Date dob, String email,
                    String phoneNumber, String address, Date hireDate, double salary,
                    int idRole, Integer idStore, Integer idWarehouse, String status, String role, String workplace) {
        setFirstName(firstName);
        setLastName(lastName);
        setGender(gender);
        setDob(dob);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
        setHireDate(hireDate);
        setSalary(salary);
        setIdRole(idRole);

        // Kiểm tra xem idStore có phải là null hay không
        if (idStore != null) {
            setIdStore(idStore);
        } else {
            setIdStore(0); // Thiết lập giá trị mặc định cho idStore
        }

        // Kiểm tra xem idWarehouse có phải là null hay không
        if (idWarehouse != null) {
            setIdWarehouse(idWarehouse);
        } else {
            setIdWarehouse(0); // Thiết lập giá trị mặc định cho idWarehouse
        }

        setStatus(status);
        setRole(role);
        setWorkplace(workplace);
    }



    // Getters and property methods
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public SimpleStringProperty firstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public SimpleStringProperty lastNameProperty() {
        return lastName;
    }

    public boolean isGender() {
        return gender.get();
    }

    public SimpleBooleanProperty genderProperty() {
        return gender;
    }

    public Date getDob() {
        return dob.get();
    }

    public ObjectProperty<Date> dobProperty() {
        return dob;
    }

    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public SimpleStringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public Date getHireDate() {
        return hireDate.get();
    }

    public ObjectProperty<Date> hireDateProperty() {
        return hireDate;
    }

    public double getSalary() {
        return salary.get();
    }

    public SimpleDoubleProperty salaryProperty() {
        return salary;
    }

    public int getIdRole() {
        return idRole.get();
    }

    public SimpleIntegerProperty idRoleProperty() {
        return idRole;
    }

    public Integer getIdStore() {
        return idStore.get();
    }

    public SimpleIntegerProperty idStoreProperty() {
        return idStore;
    }

    public Integer getIdWarehouse() {
        return idWarehouse.get();
    }

    public SimpleIntegerProperty idWarehouseProperty() {
        return idWarehouse;
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public String getWorkplace() {
        return workplace.get();
    }

    public StringProperty workplaceProperty() {
        return workplace;
    }

    // Setters
    public void setId(int id) {
        this.id.set(id);
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public void setGender(boolean gender) {
        this.gender.set(gender);
    }

    public void setDob(Date dob) {
        this.dob.set(dob);
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public void setHireDate(Date hireDate) {
        this.hireDate.set(hireDate);
    }

    public void setSalary(double salary) {
        this.salary.set(salary);
    }

    public void setIdRole(int idRole) {
        this.idRole.set(idRole);
    }

    public void setIdStore(int idStore) {
        this.idStore.set(idStore);
    }

    public void setIdWarehouse(int idWarehouse) {
        this.idWarehouse.set(idWarehouse);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public void setWorkplace(String workplace) {
        this.workplace.set(workplace);
    }
}
