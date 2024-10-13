package entity;

public class Account {
    private int id;
    private String name;
    private String role;
    private String username;
    private String password;

    public Account(int id, String name, String role, String username, String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.username = username;
        this.password = password;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
