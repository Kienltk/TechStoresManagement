package controller;

public class Session {
    private static boolean loggedIn = false;
    private static String role;
    private static int idStore;
    private static int idWarehouse;
    private static String employeeName;

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean status) {
        loggedIn = status;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        Session.role = role;
    }

    public static int getIdStore() {
        return idStore;
    }

    public static void setIdStore(int idStore) {
        Session.idStore = idStore;
    }

    public static int getIdWarehouse() {
        return idWarehouse;
    }

    public static void setIdWarehouse(int idWarehouse) {
        Session.idWarehouse = idWarehouse;
    }

    public static String getEmployeeName() {
        return employeeName;
    }

    public static void setEmployeeName(String employeeName) {
        Session.employeeName = employeeName;
    }
}
