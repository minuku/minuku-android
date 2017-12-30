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

import labelingStudy.nctu.minuku.BuildConfig;

/**
 * Created by shriti on 7/17/16.
 */
public class Constants {

    static final Calendar c = Calendar.getInstance();

    public static final String YES = "YES";
    public static final String NO = "NO";

    public static int Day = c.get(Calendar.DAY_OF_MONTH);
    public static int Year = c.get(Calendar.YEAR);
    public static int Month = c.get(Calendar.MONTH) + 1;

    //sharedPrefS
    public static final String sharedPrefString = "edu.nctu.minuku_2";

    // Firebase config
    public static final String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_ROOT_URL;
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/users";
    public static final String FIREBASE_URL_MOODS = FIREBASE_URL + "/moods";
    public static final String FIREBASE_URL_NOTES = FIREBASE_URL + "/notes";
    public static final String FIREBASE_URL_NOTIFICATIONS = FIREBASE_URL + "/notifications";
    public static final String FIREBASE_URL_IMAGES = FIREBASE_URL + "/photos";
    public static final String FIREBASE_URL_LOCATION = FIREBASE_URL + "/location";
    public static final String FIREBASE_URL_SEMANTIC_LOCATION = FIREBASE_URL + "/semantic_location";
    public static final String FIREBASE_URL_QUESTIONS = FIREBASE_URL + "/questions";
    public static final String FIREBASE_URL_MCQ = FIREBASE_URL_QUESTIONS + "/mcq";
    public static final String FIREBASE_URL_FREE_RESPONSE = FIREBASE_URL_QUESTIONS + "/freeresponse";
    public static final String FIREBASE_URL_USER_SUBMISSION_STATS = FIREBASE_URL + "/submissionstats";
    public static final String FIREBASE_URL_DIABETESLOG = FIREBASE_URL + "/diabetes_log";
    public static final String FIREBASE_URL_EOD_QUESTION_ANSWER = FIREBASE_URL + "/EOD_question_answer";
    public static final String FIREBASE_URL_TAG = FIREBASE_URL + "/tags";
    public static final String FIREBASE_URL_TAG_RECENT = FIREBASE_URL + "/recent_tags";
    public static final String FIREBASE_URL_TIMELINE_PATCH = FIREBASE_URL + "/eod_timeline_notes";
    public static final String FIREBASE_URL_MISSED_REPORT_PROMPT_QNA = FIREBASE_URL + "/missed_report_prompt_QnA";
    public static final String FIREBASE_URL_DIARYSCREENSHOT = FIREBASE_URL + "/diary_screenshot";




    // Provider stuff
    public static final String GOOGLE_AUTH_PROVIDER = "google";
    public static final String PASSWORD_PROVIDER = "password";
    //public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";

    // Google provider hashkeys
    public static final String GGL_PROVIDER_USERNAME_KEY = "username";
    public static final String GGL_PROVIDER_EMAIL_KEY = "email";

    // Shared pref ids
    public static final String ID_SHAREDPREF_EMAIL = "email";
    public static final String ID_SHAREDPREF_PROVIDER = "provider";
    //public static final String ID_SHAREDPREF_DISPLAYNAME = "displayName";

    public static final String KEY_SIGNUP_EMAIL = "SIGNUP_EMAIL";
    public static final String KEY_ENCODED_EMAIL = "ENCODED_EMAIL";

    public static final String LOG_ERROR = "Error:";


    // Prompt service related constants
    public static final int PROMPT_SERVICE_REPEAT_MILLISECONDS = 1000 * 10; // 1000 * 60 = 1 minute
    //changing from 50 mins to 15 mins, users were getting it close to bedtime
    public static final int DIARY_NOTIFICATION_SERVICE_REPEAT_MILLISECONDS = 15 * 60 * 1000; //15 minutes


    // Notification related constants
    public static final String CAN_SHOW_NOTIFICATION = "ENABLE_NOTIFICATIONS";

    public static final String MOOD_REMINDER_TITLE = "How are you feeling right now?";
    public static final String MOOD_REMINDER_MESSAGE = "Tap here to report your mood.";

    public static final String MOOD_ANNOTATION_TITLE = "Tell us more about your mood";
    public static final String MOOD_ANNOTATION_MESSAGE = "Tap here answer a quick question.";

    public static final String MISSED_ACTIVITY_DATA_PROMPT_TITLE = "We want to hear from you!";
    public static final String MISSED_ACTIVITY_DATA_PROMPT_MESSAGE = "Tap here to answer some questions.";

    public static final String EOD_DIARY_PROMPT_TITLE = "Diary entry";
    public static final String EOD_DIARY_PROMPT_MESSAGE = "Tap here to complete today's diary.";

    public static final int CONTEXT_SOURCE_INVALID_VALUE_INTEGER = -9999;
    public static final long CONTEXT_SOURCE_INVALID_VALUE_LONG_INTEGER = -9999;
    public static final long CONTEXT_SOURCE_INVALID_VALUE_FLOAT = -9999;
    public static final int SENSOR_QUEUE_SIZE = 20;


    //default queue size
    public static final int DEFAULT_QUEUE_SIZE = 20;

    //specific queue sizes
    public static final int LOCATION_QUEUE_SIZE = 50;
    public static final int IMAGE_QUEUE_SIZE = 20;
    public static final int MOOD_QUEUE_SIZE = 20;

    public static final int MOOD_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 15;
    public static final int IMAGE_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 30;
    public static final int FOOD_IMAGE_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 180;

    public static final int MOOD_NOTIFICATION_EXPIRATION_TIME = 30 * 60 /* 30 minutes*/;
    //changing missed report notification expiry to 2 hours as users are missing
    public static final int MISSED_REPORT_NOTIFICATION_EXPIRATION_TIME =  2 * 60 * 60 /* 120 minutes*/;
    //changing diary notification expiry to 2 hours as users are missing it
    public static final int DIARY_NOTIFICATION_EXPIRATION_TIME = 2 * 60 * 60 /* 120 minutes*/;

    public static final String TAPPED_NOTIFICATION_ID_KEY = "TAPPED_NOTIFICATION_ID" ;
    public static final String SELECTED_LOCATIONS = "USERPREF_SELECTED_LOCATIONS";
    public static final String BUNDLE_KEY_FOR_QUESTIONNAIRE_ID = "QUESTIONNAIRE_ID";
    public static final String BUNDLE_KEY_FOR_NOTIFICATION_SOURCE = "NOTIFICATION_SOURCE";
    public static final String APP_NAME = "LS";
    public static final String RUNNING_APP_DECLARATION = APP_NAME + " is running in the background";
    public static final long INTERNAL_LOCATION_UPDATE_FREQUENCY = 1 * 10 * 1000; // 1 * 300 * 1000
    public static final long INTERNAL_LOCATION_LOW_UPDATE_FREQUENCY = 1 * 20 * 1000; // 1 * 300 * 1000

    public static final float LOCATION_MINUMUM_DISPLACEMENT_UPDATE_THRESHOLD = 50 ;

    public static final String DIABETES_LOG_NOTIFICATION_SOURCE = "DIABETES_LOG";

    /* from NCTU */
    public static String current_timer_state_tag = "current"; //for getTag

    public static String current_timer_state = "home";

    public final static String DATE_FORMAT_NOW = "yyyy/MM/dd HH:mm:ss";//yyyy-MM-dd HH:mm:ss Z
    public final static String DATE_FORMAT_for_storing = "yyyy-MM-dd HH:mm:ss";

    public final static String home_tag = "home";
    public final static String timer_move_tag = "timer_move";

    public static String DEVICE_ID = "NA";
    public static String USER_ID = "N";
    public static String GROUP_NUM = "A";
    public static int TaskDayCount = -1;

    public static String Interval_Sample = "Interval_Sample";

    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;

    public static long MILLISECONDS_PER_SECOND = 1000;
    public static final long MILLISECONDS_PER_MINUTE = SECONDS_PER_MINUTE*MILLISECONDS_PER_SECOND;

    public static boolean tabpos = false;

    public static final String ACTIVITY_DELIMITER = ":";

}
