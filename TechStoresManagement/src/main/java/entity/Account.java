package entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class Account {

    private  IntegerProperty id;
    private  StringProperty name;
    private  StringProperty username;
    private  StringProperty password;
    private  StringProperty role;

    public Account(int id, String name, String username, String password) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
    }

    public Account(int id, String name, String username, String password, String role) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
    }



    // Getters và setters cho các thuộc tính
    public int getId() {
        return id.get();
    }

    public String getRole() {
        return role.get();
    }

    public StringProperty roleProperty() {
        return role;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty passwordProperty() {
        return password;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name=" + name +
                ", username=" + username +
                ", password=" + password +
                ", role=" + role +
                '}';
    }
}
