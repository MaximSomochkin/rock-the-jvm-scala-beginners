import java.util.List;

abstract class MobDevice {
}

record CellPhone(String name, boolean camera) {
}

public class Main {

    public String monthName(int i) {
        return switch (i) {
            case 1  -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "Invalid month";
        };
    }

    public static void main(String[] args) {

    }
}
