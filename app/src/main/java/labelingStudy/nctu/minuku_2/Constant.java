package labelingStudy.nctu.minuku_2;
/**
 * Created by Lawrence on 2017/4/22.
 */

public class Constant {

    //TODO final var can put in String.xml
    public static String current_timer_state_tag = "current"; //for getTag

    public static String current_timer_state = "home";

    public final static String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss Z";
    public final static String home_tag = "home";
    public final static String timer_move_tag = "timer_move";

//    public static String DEVICE_ID = "NA";

    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;

    public static long MILLISECONDS_PER_SECOND = 1000;
    public static final long MILLISECONDS_PER_MINUTE = SECONDS_PER_MINUTE*MILLISECONDS_PER_SECOND;

    public static boolean tabpos = false;

    public static final String ACTIVITY_DELIMITER = ":";


}
