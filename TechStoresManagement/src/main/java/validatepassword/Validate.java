package validatepassword;

public class Validate {
    public static String validatePassword(String password) {
        if (password.isEmpty()) {
            return "Password is required.";
        } else if (!password.matches(".*[!@#$%^&*.].*")) {
            return "Password must contain at least one special character.";
        } else if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        } else if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number.";
        } else if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }
        return "";
    }
}
