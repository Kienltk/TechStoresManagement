package entity;

import javafx.beans.property.*;
import java.sql.Date;

public class Employee {
    private  SimpleIntegerProperty id = new SimpleIntegerProperty(this, "id", 0);
    private  SimpleStringProperty firstName = new SimpleStringProperty(this, "firstName", "");
    private  SimpleStringProperty lastName = new SimpleStringProperty(this, "lastName", "");
    private  SimpleBooleanProperty gender = new SimpleBooleanProperty(this, "gender", false);
    private  SimpleObjectProperty<Date> dob = new SimpleObjectProperty<>(this, "dob", null);
    private  SimpleStringProperty email = new SimpleStringProperty(this, "email", "");
    private  SimpleStringProperty phoneNumber = new SimpleStringProperty(this, "phoneNumber", "");
    private  SimpleStringProperty address = new SimpleStringProperty(this, "address", "");
    private  SimpleObjectProperty<Date> hireDate = new SimpleObjectProperty<>(this, "hireDate", null);
    private  SimpleDoubleProperty salary = new SimpleDoubleProperty(this, "salary", 0.0);
    private  SimpleIntegerProperty idRole = new SimpleIntegerProperty(this, "idRole", 0);
    private  SimpleIntegerProperty idStore = new SimpleIntegerProperty(this, "idStore", 0);
    private  SimpleIntegerProperty idWarehouse = new SimpleIntegerProperty(this, "idWarehouse", 0);
    private  SimpleStringProperty role = new SimpleStringProperty(this, "role", "");
    private  SimpleStringProperty workplace = new SimpleStringProperty(this, "workplace", "");

    // No-argument constructor
    public Employee() {
        this.id = new SimpleIntegerProperty();
        this.firstName = new SimpleStringProperty();
        this.lastName = new SimpleStringProperty();
        this.gender = new SimpleBooleanProperty();
        this.dob = new SimpleObjectProperty<>();
        this.email = new SimpleStringProperty();
        this.phoneNumber = new SimpleStringProperty();
        this.address = new SimpleStringProperty();
        this.hireDate = new SimpleObjectProperty<>();
        this.salary = new SimpleDoubleProperty();
        this.idRole = new SimpleIntegerProperty();
        this.idStore = new SimpleIntegerProperty();
        this.idWarehouse = new SimpleIntegerProperty();
        this.role = new SimpleStringProperty();
        this.workplace = new SimpleStringProperty();
    }

    // Parameterized constructor
    public Employee(int id, String firstName, String lastName, boolean gender, Date dob, String email,
                    String phoneNumber, String address, double salary, int idRole, int idStore,
                    int idWarehouse, String role, String workplace) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setGender(gender);
        setDob(dob);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
        setSalary(salary);
        setIdRole(idRole);
        setIdStore(idStore);
        setIdWarehouse(idWarehouse);
        setRole(role);
        setWorkplace(workplace);
    }

    public Employee(int id, String firstName, String lastName, boolean gender, Date dob, String email,
                    String phoneNumber, String address, double salary, int idRole, int idWarehouse, String role,
                    String workplace) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setGender(gender);
        setDob(dob);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
        setSalary(salary);
        setIdRole(idRole);
        setIdWarehouse(idWarehouse);
        setRole(role);
        setWorkplace(workplace);
    }



    // Getters and property methods
    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }

    public String getFirstName() { return firstName.get(); }
    public SimpleStringProperty firstNameProperty() { return firstName; }

    public String getLastName() { return lastName.get(); }
    public SimpleStringProperty lastNameProperty() { return lastName; }

    public boolean isGender() { return gender.get(); }
    public SimpleBooleanProperty genderProperty() { return gender; }

    public Date getDob() { return dob.get(); }
    public ObjectProperty<Date> dobProperty() { return dob; }

    public String getEmail() { return email.get(); }
    public SimpleStringProperty emailProperty() { return email; }

    public String getPhoneNumber() { return phoneNumber.get(); }
    public SimpleStringProperty phoneNumberProperty() { return phoneNumber; }

    public String getAddress() { return address.get(); }
    public SimpleStringProperty addressProperty() { return address; }

    public Date getHireDate() { return hireDate.get(); }
    public ObjectProperty<Date> hireDateProperty() { return hireDate; }

    public double getSalary() { return salary.get(); }
    public SimpleDoubleProperty salaryProperty() { return salary; }

    public int getIdRole() { return idRole.get(); }
    public SimpleIntegerProperty idRoleProperty() { return idRole; }

    public int getIdStore() { return idStore.get(); }
    public SimpleIntegerProperty idStoreProperty() { return idStore; }

    public int getIdWarehouse() { return idWarehouse.get(); }
    public SimpleIntegerProperty idWarehouseProperty() { return idWarehouse; }

    public String getRole() { return role.get(); }
    public StringProperty roleProperty() { return role; }

    public String getWorkplace() { return workplace.get(); }
    public StringProperty workplaceProperty() { return workplace; }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    public void setGender(boolean gender) { this.gender.set(gender); }
    public void setDob(Date dob) { this.dob.set(dob); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber.set(phoneNumber); }
    public void setAddress(String address) { this.address.set(address); }
    public void setHireDate(Date hireDate) { this.hireDate.set(hireDate); }
    public void setSalary(double salary) { this.salary.set(salary); }
    public void setIdRole(int idRole) { this.idRole.set(idRole); }
    public void setIdStore(int idStore) { this.idStore.set(idStore); }
    public void setIdWarehouse(int idWarehouse) { this.idWarehouse.set(idWarehouse); }
    public void setRole(String role) { this.role.set(role); }
    public void setWorkplace(String workplace) { this.workplace.set(workplace); }
}
