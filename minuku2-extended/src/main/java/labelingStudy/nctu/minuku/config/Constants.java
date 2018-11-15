/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku.config;

import java.util.Calendar;

/**
 * Created by shriti on 7/17/16.
 */
public class Constants {

    static final Calendar c = Calendar.getInstance();

    public static String currentWork;

    public static int siteRange = 25;

    public static final String ACTION_CONNECTIVITY_CHANGE = "CONNECTIVITY_CHANGE";

    public static final String ONGOING_CHANNEL_NAME = "LS";
    public static final String ONGOING_CHANNEL_ID = "LabelingStudy_id";
    public static final String SURVEY_CHANNEL_NAME = "LS";
    public static final String SURVEY_CHANNEL_ID = "Survey_id";

    public static final String CHECK_SERVICE_ACTION = "checkService";

    public static final long MILLISECONDS_PER_SECOND = 1000;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final long MILLISECONDS_PER_DAY = HOURS_PER_DAY *MINUTES_PER_HOUR*SECONDS_PER_MINUTE*MILLISECONDS_PER_SECOND;
    public static final long MILLISECONDS_PER_HOUR = MINUTES_PER_HOUR*SECONDS_PER_MINUTE*MILLISECONDS_PER_SECOND;
    public static final long MILLISECONDS_PER_MINUTE = SECONDS_PER_MINUTE*MILLISECONDS_PER_SECOND;
    public final static String DATE_FORMAT_NOW_Dash = "yyyy-MM-dd HH:mm:ss Z";
    public final static String DATE_FORMAT_NOW_SLASH = "yyyy/MM/dd HH:mm:ss Z";
    public final static String DATE_FORMAT_NOW_MINUTE_SLASH = "yyyy/MM/dd HH:mm";
    public static final String DATE_FORMAT_NOW_NO_ZONE_Slash = "yyyy/MM/dd HH:mm:ss";
    public static final String DATE_FORMAT_NOW_DAY_Slash = "yyyy/MM/dd";
    public static final String DATE_FORMAT_NOW_NO_ZONE = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_NOW_DAY = "yyyy-MM-dd";
    public static final String DATE_FORMAT_NOW_HOUR = "yyyy-MM-dd HH";
    public static final String DATE_FORMAT_NOW_HOUR_MIN = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_NOW_HOUR_MIN_AMPM = "yyyy-MM-dd hh:mm a";
    public static final String DATE_FORMAT_NOW_AMPM_HOUR_MIN = "yyyy-MM-dd a hh:mm";
    public static final String DATE_FORMAT_HOUR_MIN_SECOND = "HH:mm:ss";
    public static final String DATE_FORMAT_FOR_ID = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_HOUR_MIN = "HH:mm";
    public static final String DATE_FORMAT_HOUR = "HH";
    public static final String DATE_FORMAT_MIN = "mm";
    public static final String DATE_FORMAT_AMPM_HOUR_MIN = "a hh:mm";
    public static final String DATE_FORMAT_HOUR_MIN_AMPM = "hh:mm a";
    public static final String DATE_FORMAT_Small_HOUR_MIN = "hh:mm";
    public static final String DATE_FORMAT_DATE_TEXT = "MMM dd";
    public static final String DATE_FORMAT_DATE_TEXT_HOUR_MIN = "MMM dd HH:mm";
    public static final String DATE_FORMAT_DATE_TEXT_HOUR_MIN_SEC = "MMM dd  HH:mm:ss";
    public static final int DATA_FORMAT_TYPE_NOW=0;
    public static final int DATA_FORMAT_TYPE_DAY=1;
    public static final int DATA_FORMAT_TYPE_HOUR=2;

    public static final String SESSION_TYPE_DETECTED_BY_SYSTEM = "system";
    public static final String SESSION_TYPE_DETECTED_BY_USER = "user";

    public static final int SESSION_SHOULDNT_BEEN_SENT_FLAG = -1;
    public static final int SESSION_SHOULD_BE_SENT_FLAG = 0;
    public static final int SESSION_IS_ALREADY_SENT_FLAG = 1;

    public static final String DELIMITER = ";;;";
    public static final String SESSION_DELIMITER = ",";
    public static final String ACTIVITY_DELIMITER = ";;";
    public static final String CONTEXT_SOURCE_DELIMITER = ":";
    public static final String DELIMITER_IN_COLUMN = "::";

    public static final String YES = "YES";
    public static final String NO = "NO";

    public static final int INVALID_INT_VALUE = -1;
    public static final long INVALID_TIME_VALUE = -1;
    public static final String INVALID_STRING_VALUE = "NA";

    //sharedPrefs
    public static final String sharedPrefString = "labelingStudy.nctu.minuku_2";

    //file path
    public static final String PACKAGE_DIRECTORY_PATH = "/Android/data/labelingStudy.nctu.minuku_2/";

    public static final String ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY = "detected-transportation";
    public static final String ANNOTATION_TAG_DETECTED_SITENAME = "detected-sitename";
    public static final String ANNOTATION_TAG_DETECTED_SITELOCATION = "detected-sitelocation";
    public static final String ANNOTATION_TAG_Label = "Label";

    public static final String ANNOTATION_Label_TRANSPORTATOIN = "Transportation";
    public static final String ANNOTATION_Label_GOAL = "Goal";
    public static final String ANNOTATION_Label_SPECIALEVENT = "SpecialEvent";
    public static final String ANNOTATION_Label_SITENAME = "Sitename";
    public static final String ANNOTATION_Label_SITELOCATION = "SiteLocation";
    public static final String ANNOTATION_Label_TIME = "LabeledTime";

    public static final String UNKNOWN_SITE = "未知定點";

    public static final int SESSION_NEVER_GET_HIDED_FLAG = 0;
    public static final int SESSION_IS_HIDED_FLAG = 1;

    public static final String DESC = "DESC";
    public static final String ASC = "ASC";

    // Prompt service related constants
    public static final int PROMPT_SERVICE_REPEAT_MILLISECONDS = 1000 * 10; // 1000 * 60 = 1 minute

    public static final int CONTEXT_SOURCE_INVALID_VALUE_INTEGER = -9999;
    public static final long CONTEXT_SOURCE_INVALID_VALUE_LONG_INTEGER = -9999;
    public static final long CONTEXT_SOURCE_INVALID_VALUE_FLOAT = -9999;
    public static final int SENSOR_QUEUE_SIZE = 20;

    //default queue size
    public static final int DEFAULT_QUEUE_SIZE = 20;

    //specific queue sizes
    public static final int LOCATION_QUEUE_SIZE = 50;

    public static final String APP_NAME = "LS";
    public static final String APP_FULL_NAME = "Labeling Study";
    public static final String RUNNING_APP_DECLARATION = "正在執行 "+ APP_FULL_NAME;
    public static final long INTERNAL_LOCATION_UPDATE_FREQUENCY = 1 * 10 * 1000; // 1 * 300 * 1000
    public static final long INTERNAL_LOCATION_LOW_UPDATE_FREQUENCY = 1 * 60 * 1000; // 1 * 300 * 1000

    public static final float LOCATION_MINUMUM_DISPLACEMENT_UPDATE_THRESHOLD = 50 ;

    public static String current_timer_state_tag = "current"; //for getTag

    public static String current_timer_state = "home";

    public final static String DATE_FORMAT_NOW = "yyyy/MM/dd HH:mm:ss";//yyyy-MM-dd HH:mm:ss Z
    public final static String DATE_FORMAT_for_storing = "yyyy-MM-dd HH:mm:ss";

    public final static String home_tag = "home";

    public static boolean tabpos = false;

    public static String DEVICE_ID = "NA";
    public static String USER_ID = "N";
    public static String GROUP_NUM = "A";
    public static int TaskDayCount = -1;

    public static final int TIMER_UPDATE_THREAD_SIZE = 1;
    public static final int MAIN_THREAD_SIZE = 2;
    public static final int STREAM_UPDATE_FREQUENCY = 10;
    public static final int STREAM_UPDATE_DELAY = 0;

    public static final int ISALIVE_UPDATE_FREQUENCY = 1 * 60 * 60;
    public static final int ISALIVE_UPDATE_DELAY = 0;

    public static final String ACTIVITY_CONFIDENCE_CONNECTOR = ":";

    public static int NOTIFICATION_UPDATE_THREAD_SIZE = 1;
}
