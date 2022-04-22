import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
