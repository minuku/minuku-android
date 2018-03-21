package labelingStudy.nctu.minuku.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import labelingStudy.nctu.minuku.config.Constants;

/**
 * Created by Lawrence on 2018/3/13.
 */

public class ScheduleAndSampleManager {

    public static int bedStartTime = 0;
    public static int bedEndTime = 5;
    public static int bedMiddleTime = 2;
    public static long time_base = 0;

    /**convert long to timestring**/
    public static String getTimeString(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_SLASH);
        String currentTimeString = sdf_now.format(time);

        return currentTimeString;
    }


    public static String getTimeString(long time, SimpleDateFormat sdf){

        String currentTimeString = sdf.format(time);

        return currentTimeString;
    }

    public static String getCurrentTimeString() {

        return getTimeString(getCurrentTimeInMillis());
    }


    /**get the current time in milliseconds**/
    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }

    public static long getTimeInMillis(String givenDateFormat, SimpleDateFormat sdf){
        long timeInMilliseconds = 0;
        try {
            Date mDate = sdf.parse(givenDateFormat);
            timeInMilliseconds = mDate.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeInMilliseconds;
    }

    public static int getHourOfTimeOfDay (String TimeOfDay){

        return Integer.parseInt(TimeOfDay.split(":")[0] ) ;
    }

    public static int getMinuteOfTimeOfDay (String TimeOfDay){

        return Integer.parseInt(TimeOfDay.split(":")[1] );
    }

}
