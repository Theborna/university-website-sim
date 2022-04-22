
public class Movement {
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
