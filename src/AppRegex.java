import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum AppRegex {
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