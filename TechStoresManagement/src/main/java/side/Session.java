package side;

public class Session {
    private static boolean loggedIn = false;

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setLoggedIn(boolean status) {
        loggedIn = status;
    }
}
