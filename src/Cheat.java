
public class Cheat {
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
