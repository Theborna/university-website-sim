import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Student implements User {
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
