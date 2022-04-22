import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class App {
    private static Scanner sc = new Scanner(System.in);
    private static boolean logged;
    private static Map<String, Integer> datesMap = Stream.of(new Object[][] {
            { "01", 31 }, { "02", 29 }, { "03", 31 }, { "04", 30 }, { "05", 31 }, { "06", 30 },
            { "07", 31 }, { "08", 31 }, { "09", 30 }, { "10", 31 }, { "11", 30 }, { "12", 31 }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));

    private static void parse(String input) {
        if (input.length() == 0)
            return;
        if (input.startsWith("register "))
            Database.register(input);
        else if (input.startsWith("login ")) {
            try {
                Manager.login(input);
                logged = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (input.startsWith("logout")) {
            Manager.logout();
            logged = false;
        } else if (logged && !input.startsWith("login ") && !input.startsWith("data clear"))
            Manager.parseData(input);
        else
            System.out.println("invalid command");
    }

    public static void main(String[] args) throws Exception {
        String input = sc.nextLine();
        while (!input.equals("exit")) {
            parse(input.trim());
            input = sc.nextLine();
        }
    }

    public static String getNextLine() throws IllegalArgumentException {
        String ans = sc.nextLine().trim();
        if (ans.equals("end") || ans.equals("exit"))
            throw new IllegalArgumentException();
        while (!AppRegex.validStudentEntry(ans)) {
            System.out.println("invalid command");
            ans = App.getNextLine();
        }
        return ans;
    }

    public static Integer dateAsInt(String date) throws Exception {
        String year = date.split("/")[0], month = date.split("/")[1], day = date.split("/")[2];
        if (datesMap.get(month) < Integer.parseInt(day))
            throw new Exception("invalid command");
        return Integer.parseInt(day) + 100 * Integer.parseInt(month) + 100 * 100 * Integer.parseInt(year);
    }

    public static String intAsDate(int date) {
        return String.valueOf(date / (100 * 100)) + (((date / 100) % 100) >= 10 ? "/" : "/0")
                + String.valueOf((date / 100) % 100) + ((date % 100 >= 10) ? "/" : "/0") + String.valueOf(date % 100);
    }

    public static Integer nextWeekDate(String date) throws Exception {
        String year = date.split("/")[0], month = date.split("/")[1], day = date.split("/")[2];
        int _year = Integer.parseInt(year), _month = Integer.parseInt(month), _day = Integer.parseInt(day);
        if (datesMap.get(month) < Integer.parseInt(day))
            throw new Exception("invalid command");
        if (Integer.parseInt(day) + 7 > datesMap.get(month)) {
            _day = -datesMap.get(month) + Integer.parseInt(day) + 7;
            if (++_month > 12) {
                _month = 1;
                _year++;
            }
        } else
            _day += 7;
        return _day + 100 * _month + 100 * 100 * _year;
    }
}

enum AppRegex {
    REGISTER("register (?<username>.+) (?<password>.+) (?<type>.+)"),
    WRIST_UP_DATE("wrist-up\\d (?<count>\\d+) (?<startDate>\\S+) (?<endDate>\\S+)"),
    WRIST_UP("wrist-up\\d (?<count>\\d+)"),
    MOVE("(?<personId>\\d+) (?<sourceExam>\\d+) (?<destinationExam>\\d+) (?<date>\\S+)"),
    CHEAT_ATTEMPT("(?<personId>\\d+) (?<cheatResult>\\w+) (?<examId>\\d+) (?<date>\\S+)"),
    WRIST_4("wrist-up4 (?<examId>\\d+) (?<count>\\d+)"),
    WRIST_5("wrist-up5 (?<count>\\d+)"),
    STUDENT1("[\\d ]+"), STUDENT2("\\d+"), STUDENT3("\\d+ \\D+ \\d+ \\S+"), STUDENT4("\\d+ \\d+ \\d+ \\S+");

    String regex;

    AppRegex(String regex) {
        this.regex = regex;
    }

    public static Matcher getMatcher(String input, AppRegex command) {
        Matcher matcher = Pattern.compile(command.regex).matcher(input);
        if (matcher.matches())
            return matcher;
        return null;
    }

    public static boolean validStudentEntry(String entry) {
        if ((getMatcher(entry, STUDENT1) != null) || (getMatcher(entry, STUDENT2) != null)
                || (getMatcher(entry, STUDENT3) != null) || (getMatcher(entry, STUDENT4) != null))
            return true;
        return false;
    }

}

class Cheat {
    public int personId, examId, date;
    public boolean success;

    public Cheat(int personId, int examId, int date, String success) throws Exception {
        this.personId = personId;
        this.examId = examId;
        this.date = date;
        if (!success.equals("positive") && !success.equals("negative"))
            throw new Exception("invalid format");
        this.success = success.equals("positive");
    }

    @Override
    public String toString() {
        return "{" +
                " personId='" + personId + "'" +
                ", examId='" + examId + "'" +
                ", date='" + date + "'" +
                ", success='" + success + "'" +
                "}";
    }
}

class Database {

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

class Manager {
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

class Movement {
    public int destinationExam, sourceExam, date, personId;

    public Movement(int destinationExam, int sourceExam, int date, int personId) {
        this.destinationExam = destinationExam;
        this.sourceExam = sourceExam;
        this.date = date;
        this.personId = personId;
    }

    @Override
    public String toString() {
        return "{" +
                " destinationExam='" + destinationExam + "'" +
                ", sourceExam='" + sourceExam + "'" +
                ", date='" + date + "'" +
                ", personId='" + personId + "'" +
                "}";
    }

}

class Student implements User {
    private String username, password;
    private int m, p, t, id;

    public Student(String username, String password, int id) {
        this.username = username;
        this.password = password;
        this.id = id;
    }

    public void parse(String input) throws IllegalArgumentException {
        if (input.startsWith("nextround"))
            input = App.getNextLine();
        if (input.equals("end") || input.equals("exit"))
            throw new IllegalArgumentException();
        while (!AppRegex.validStudentEntry(input)) {
            System.out.println("invalid command");
            input = App.getNextLine();
        }
        Matcher matcher;
        int n = Integer.parseInt(input);
        Database.addN(n);
        m = Integer.parseInt(App.getNextLine());
        for (int i = 0; i < m; i++)
            Database.addCheatgroup(Arrays.asList(App.getNextLine().split(" ")).stream()
                    .map(j -> Integer.parseInt(j)).collect(Collectors.toList()));
        Database.addC(Integer.parseInt(App.getNextLine()));
        p = Integer.parseInt(App.getNextLine());
        for (int i = 0; i < p; i++) {
            while ((matcher = AppRegex.getMatcher(App.getNextLine(), AppRegex.MOVE)) == null)
                System.out.println("invalid command");
            String personId = matcher.group("personId"), sourceExam = matcher.group("sourceExam"),
                    destinationExam = matcher.group("destinationExam"), date = matcher.group("date");
            Database.addMovement(Integer.parseInt(personId), Integer.parseInt(destinationExam),
                    Integer.parseInt(sourceExam), date);
        }
        t = Integer.parseInt(App.getNextLine());
        for (int i = 0; i < t; i++) {
            String line = App.getNextLine();
            while ((matcher = AppRegex.getMatcher(line, AppRegex.CHEAT_ATTEMPT)) == null)
                System.out.println("invalid command");
            String personId = matcher.group("personId"), examId = matcher.group("examId"),
                    cheatResult = matcher.group("cheatResult"),
                    date = matcher.group("date");
            Database.addCheat(Integer.parseInt(personId), cheatResult, Integer.parseInt(examId), date);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Student [username=" + username + ", password=" + password + "]";
    }

    public int getId() {
        return id;
    }
}

class Teacher implements User {
    private String username, password;
    private final static int COUNT = 0, START_DATE = 1, END_DATE = 2;
    private final static String _START_DATE = "0000/01/01", _END_DATE = "9999/12/31";

    public Teacher(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void parse(String input) throws IllegalArgumentException {
        if (input.startsWith("wrist-up")) {
            try {
                char type = input.charAt(8);
                String[] allCommands = null;
                if (Integer.valueOf(type) - Integer.valueOf('0') < 4)
                    allCommands = getCountAndDate(input);
                switch (type) {
                    case '1':
                        wristOne(allCommands[COUNT], allCommands[START_DATE], allCommands[END_DATE]);
                        break;
                    case '2':
                        wristTwo(allCommands[COUNT], allCommands[START_DATE], allCommands[END_DATE]);
                        break;
                    case '3':
                        wristThree(allCommands[COUNT], allCommands[START_DATE], allCommands[END_DATE]);
                        break;
                    case '4':
                        wristFour(input);
                        break;
                    case '5':
                        wristFive(input);
                        break;
                    default:
                        throw new IllegalArgumentException("invalid command");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (input.equals("clear data"))
            Database.clearData();
        else if (!input.startsWith("end"))
            throw new IllegalArgumentException("invalid command");
    }

    private void wristOne(String count, String startDate, String endDate) throws Exception {
        System.out.println("wrist-up1");
        Map<Integer, Integer> wrongDoings = new HashMap<>();
        for (int i = 0; i < Database.getN(); i++)
            wrongDoings.put(i + 1, 0);
        for (Movement move : Database.getMovements())
            if (move.date > App.dateAsInt(startDate) && move.date < App.dateAsInt(endDate))
                wrongDoings.replace(move.personId,
                        wrongDoings.get(move.personId) + Database.getSus().get(move.personId).wrongDoings(move.date));
        printSortedMap(wrongDoings, count, false);
    }

    private void wristTwo(String count, String startDate, String endDate) throws Exception {
        System.out.println("wrist-up2");
        Map<Integer, Integer> allCheats = new HashMap<>();
        for (int i = 0; i < Database.getN(); i++)
            allCheats.put(i + 1, 0);
        for (Cheat cheat : Database.getCheatAttempts())
            if (cheat.success && (cheat.date < App.dateAsInt(endDate) && cheat.date > App.dateAsInt(startDate)))
                allCheats.replace(cheat.personId, allCheats.get(cheat.personId) + 1);
        printSortedMap(allCheats, count, false);
    }

    private void wristThree(String count, String startDate, String endDate) throws Exception {
        System.out.println("wrist-up3");
        Map<Integer, Integer> allCheats = new HashMap<>();
        for (int i = 0; i < Database.getC(); i++)
            allCheats.put(i + 1, 0);
        for (Cheat cheat : Database.getCheatAttempts())
            if (cheat.success && (cheat.date < App.dateAsInt(endDate) && cheat.date > App.dateAsInt(startDate)))
                allCheats.replace(cheat.examId, allCheats.get(cheat.examId) + 1);
        printSortedMap(allCheats, count, false);
    }

    private void wristFour(String input) throws Exception {
        Matcher m;
        if ((m = AppRegex.getMatcher(input, AppRegex.WRIST_4)) == null)
            throw new Exception("no such command");
        int count = Integer.parseInt(m.group("count")), examId = Integer.parseInt(m.group("examId"));
        System.out.println("wrist-up4");
        Map<Integer, Integer> datesCheated = new HashMap<>();
        for (Cheat cheat : Database.getCheatAttempts())
            if (cheat.examId == examId)
                if (datesCheated.containsKey(cheat.date))
                    datesCheated.replace(cheat.date, datesCheated.get(cheat.date) + (cheat.success ? 1 : 0));
                else
                    datesCheated.put(cheat.date, cheat.success ? 1 : 0);
        printSortedMap(datesCheated, String.valueOf(count), true);
    }

    private void wristFive(String input) throws Exception {
        Matcher m;
        if ((m = AppRegex.getMatcher(input, AppRegex.WRIST_5)) == null)
            throw new Exception("no such command");
        String count = (m.group("count"));
        System.out.println("wrist-up5");
        Map<Integer, Integer> allDates = new HashMap<Integer, Integer>();
        int minDate = App.dateAsInt(_END_DATE), maxDate = 0;
        for (int i = 0; i < Database.getN(); i++)
            if (Database.getSus().containsKey(i)) {
                Suspicions sus = Database.getSus().get(i);
                minDate = (sus.minDate() > minDate) ? minDate : sus.minDate();
                maxDate = (sus.maxDate() < maxDate) ? maxDate : sus.maxDate();
            }
        for (int i = minDate; i <= maxDate; i++)
            allDates.put(i, 0);
        for (Integer i : allDates.keySet())
            for (Suspicions sus : Database.getSus().values())
                if (sus.isSuspicious(i))
                    allDates.replace(i, allDates.get(i) + 1);
        printSortedMap(allDates, count, true);
    }

    private void printSortedMap(Map<Integer, Integer> map, String count, boolean asDate) {
        map = map.entrySet().stream().sorted(
                (i, j) -> (j.getValue().compareTo(i.getValue()) != 0) ? j.getValue().compareTo(i.getValue())
                        : i.getKey().compareTo(j.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        for (int i = 0; (i < Integer.parseInt(count)) && (i < map.size()); i++)
            System.out.println((asDate ? App.intAsDate((Integer) map.keySet().toArray()[i])
                    : map.keySet().toArray()[i]) + " " + map.values().toArray()[i]);
    }

    private String[] getCountAndDate(String input) throws Exception {
        Matcher m;
        String startDate = _START_DATE, endDate = _END_DATE;
        if ((m = AppRegex.getMatcher(input, AppRegex.WRIST_UP_DATE)) != null) {
            startDate = m.group("startDate");
            endDate = m.group("endDate");
        } else if ((m = AppRegex.getMatcher(input, AppRegex.WRIST_UP)) == null)
            throw new IllegalArgumentException("no such command");
        return new String[] { m.group("count"), startDate, endDate };
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Teacher [username=" + username + ", password=" + password + "]";
    }
}

interface User {
    public void parse(String input);

    public String getUsername();

    public String getPassword();

}

class Suspicions {
    private ArrayList<List<Integer>> dates = new ArrayList<>();

    public void addSuspicion(int _date, Integer nextWeekDate) {
        dates.add(Arrays.asList(_date, nextWeekDate));
    }

    public boolean isSuspicious(int date) {
        for (List<Integer> i : dates)
            if (date >= i.get(0) && date <= i.get(1))
                return true;
        return false;
    }

    public int wrongDoings(int date) {
        int result = 0;
        for (List<Integer> i : dates)
            if (date >= i.get(0) && date <= i.get(1))
                result++;
        return result;
    }

    @Override
    public String toString() {
        return "Suspicions [dates=" + dates + "]";
    }

    public int minDate() {
        int minDate = dates.get(0).get(0);
        for (List<Integer> i : dates)
            if (i.get(0) < minDate)
                minDate = i.get(0);
        return minDate;
    }

    public int maxDate() {
        int maxDate = dates.get(0).get(1);
        for (List<Integer> i : dates)
            if (i.get(1) > maxDate)
                maxDate = i.get(0);
        return maxDate;
    }
}