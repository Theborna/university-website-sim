import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Teacher implements User {
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
