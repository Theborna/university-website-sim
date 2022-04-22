import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class Database {

    private static ArrayList<User> users = new ArrayList<User>();
    private static ArrayList<ArrayList<Integer>> cheatGroups = new ArrayList<ArrayList<Integer>>();
    private static ArrayList<Movement> movements = new ArrayList<>();
    private static ArrayList<Cheat> cheatAttempts = new ArrayList<>();
    private static Map<Integer, Suspicions> sus = new HashMap<>();
    private static int n, c;

    public static void register(String input) {
        try {
            Matcher m = AppRegex.getMatcher(input, AppRegex.REGISTER);
            String username = m.group(1), password = m.group(2), type = m.group(3);
            validate(username, password, type);
            if (type.equals("student"))
                users.add(new Student(username, password, users.size() + 1));
            else if (type.equals("teacher"))
                users.add(new Teacher(username, password));
            System.out.println("register successful");
            // System.out.println(users);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void validate(String username, String password, String type) throws Exception {
        Manager.validate(username, password);
        if (!type.equals("student") && !type.equals("teacher"))
            throw new IllegalStateException("no such type");
        for (User user : users)
            if (user.getUsername().equals(username))
                throw new Exception("a user exists with this username");
    }

    public static void clearData() {
        cheatGroups = new ArrayList<ArrayList<Integer>>();
        movements = new ArrayList<>();
        cheatAttempts = new ArrayList<>();
        sus = new HashMap<>();
    }

    public static User getUser(String username, String password) throws Exception {
        for (User user : users)
            if (user.getUsername().equals(username))
                if (user.getPassword().equals(password))
                    return user;
                else
                    throw new Exception("incorrect password");
        throw new Exception("no user exists with this username");
    }

    public static void addCheatgroup(List<Integer> students) {
        cheatGroups.add(new ArrayList<Integer>());
        cheatGroups.get(cheatGroups.size() - 1).addAll(students);
        // System.out.println(cheatGroups);
    }

    public static void addMovement(int personId, int destinationExam, int sourceExam, String date) {
        try {
            int _date = App.dateAsInt(date);
            movements.add(new Movement(destinationExam, sourceExam, _date, personId));
            // System.out.println(movements);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addCheat(int personId, String cheatResult, int examId, String date) {
        try {
            int _date = App.dateAsInt(date);
            cheatAttempts.add(new Cheat(personId, examId, _date, cheatResult));
            if (!sus.containsKey(personId))
                sus.put(personId, new Suspicions());
            sus.get(personId).addSuspicion(_date, App.nextWeekDate(date));
            for (ArrayList<Integer> group : cheatGroups)
                if (group.contains(personId))
                    for (Integer member : group)
                        if (member != personId) {
                            if (!sus.containsKey(member))
                                sus.put(member, new Suspicions());
                            sus.get(member).addSuspicion(_date, App.nextWeekDate(date));
                        }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static int getN() {
        return n;
    }

    public static int getC() {
        return c;
    }

    public static void addN(int n) {
        Database.n += n;
    }

    public static void addC(int c) {
        Database.c += c;
    }

    public static ArrayList<Cheat> getCheatAttempts() {
        return cheatAttempts;
    }

    public static Map<Integer, Suspicions> getSus() {
        return sus;
    }

    public static ArrayList<Movement> getMovements() {
        return movements;
    }
}
