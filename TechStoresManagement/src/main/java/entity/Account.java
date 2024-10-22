package entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class Account {

    // Các thuộc tính dùng IntegerProperty và StringProperty
    private IntegerProperty id;
    private StringProperty name;
    private StringProperty username;
    private StringProperty password;
    private StringProperty role;
    private StringProperty email;

    // Constructors

    // Constructor với 4 thuộc tính
    public Account(int id, String name, String username, String password) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
    }

    // Constructor với 5 thuộc tính
    public Account(int id, String name, String username, String password, String role) {
        this(id, name, username, password); // Sử dụng constructor khác
        this.role = new SimpleStringProperty(role);
    }

    // Constructor với 6 thuộc tính, bao gồm số điện thoại
    public Account(int id, String name, String username, String password, String role, String email) {
        this(id, name, username, password, role); // Sử dụng constructor khác
        this.email = new SimpleStringProperty(email);
    }

    // Getters và setters

    // Getter cho thuộc tính id
    public int getId() {
        return id.get();
    }

    // Setter cho thuộc tính id
    public void setId(int id) {
        this.id.set(id);
    }

    // Getter cho thuộc tính name
    public String getName() {
        return name.get();
    }

    // Setter cho thuộc tính name
    public void setName(String name) {
        this.name.set(name);
    }

    // Getter cho thuộc tính username
    public String getUsername() {
        return username.get();
    }

    // Setter cho thuộc tính username
    public void setUsername(String username) {
        this.username.set(username);
    }

    // Getter cho thuộc tính password
    public String getPassword() {
        return password.get();
    }

    // Setter cho thuộc tính password
    public void setPassword(String password) {
        this.password.set(password);
    }

    // Getter cho thuộc tính role
    public String getRole() {
        return role == null ? null : role.get(); // Tránh null pointer
    }

    // Setter cho thuộc tính role
    public void setRole(String role) {
        if (this.role == null) {
            this.role = new SimpleStringProperty(role);
        } else {
            this.role.set(role);
        }
    }

    // Getter cho thuộc tính email
    public String getEmail() {
        return email == null ? null : email.get(); // Tránh null pointer
    }

    // Setter cho thuộc tính email
    public void setEmail(String email) {
        if (this.email == null) {
            this.email = new SimpleStringProperty(email);
        } else {
            this.email.set(email);
        }
    }

    // Các Property methods để hỗ trợ binding với JavaFX
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty roleProperty() {
        return role;
    }

    public StringProperty emailProperty() {
        return email;
    }

    // Override phương thức toString để hiển thị thông tin của đối tượng
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id.get() +
                ", name=" + name.get() +
                ", username=" + username.get() +
                ", password=" + password.get() +
                (role != null ? ", role=" + role.get() : "") +
                (email != null ? ", email=" + email.get() : "") +
                '}';
    }
}
