package utils;


import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Phương thức để hash password
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Phương thức để kiểm tra password
    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
