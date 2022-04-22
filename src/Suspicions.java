import java.util.ArrayList;
import java.util.List;

public class Suspicions {
    private ArrayList<List<Integer>> dates = new ArrayList<>();

    public void addSuspicion(int _date, Integer nextWeekDate) {
        dates.add(List.of(_date, nextWeekDate));
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
