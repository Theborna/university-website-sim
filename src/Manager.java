import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Manager {
    private static User currentUser;

    public static void login(String input) throws Exception {
        Matcher m = Pattern.compile("login (?<username>.+) (?<password>.+)").matcher(input);
        m.find();
        String username = m.group(1), password = m.group(2);
        validate(username, password);
        currentUser = Database.getUser(username, password);
        System.out.println("login successful");
    }

    public static void validate(String username, String password) throws Exception {
        if (!password.matches("\\w+"))
            throw new Exception("password format is invalid");
        if (!username.matches("\\w+"))
            throw new Exception("username format is invalid");
    }

    public static void logout() {
        if (currentUser != null) {
            currentUser = null;
            System.out.println("logout successful");
        } else
            System.out.println("invalid command");
    }

    public static void parseData(String data) {
        try {
            currentUser.parse(data);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
