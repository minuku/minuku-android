package labelingStudy.nctu.minuku.Utilities;

import android.os.Environment;

import com.opencsv.CSVWriter;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.config.Constants;

/**
 * Created by Lawrence on 2018/3/31.
 */

public class CSVHelper {

    public static final String TAG = "CSVHelper";

    public static CSVWriter csv_writer = null;

    public static String file_CheckService_alive = "CheckService.csv";
    public static String file_ESM = "CheckESM.csv";
    public static String file_CAR = "CheckCAR.csv";


    public static void storeToCSV(String fileName, String... texts){

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName,true));

            List<String[]> data = new ArrayList<String[]>();

            long timestamp = ScheduleAndSampleManager.getCurrentTimeInMillis();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);


            List<String> textInList = new ArrayList<>();

            textInList.add(timeString);

            for(int index = 0; index < texts.length;index++){

                textInList.add(texts[index]);
            }

            String[] textInArray = textInList.toArray(new String[0]);

            data.add(textInArray);

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void userInformStoreToCSV(String fileName, long timestamp, JSONObject userInform){

//        String sFileName = "TransportationState.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            data.add(new String[]{timeString, userInform.toString()});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void storeToCSV_IntervalSurveyUpdated(boolean clicked){

        String sFileName = "IntervalSurveyState.csv";

//        Log.d(TAG, "sFileName : " + sFileName);

        try {

            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);

//            Log.d(TAG, "root : " + root);

            if (!root.exists()) {
                root.mkdirs();
            }

            long clickedTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            String clickedTimeString = ScheduleAndSampleManager.getTimeString(clickedTime);

            String clickedString;
            if(clicked)
                clickedString = "Yes";
            else
                clickedString = "No";

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            if(clicked)
                data.add(new String[]{"", "", clickedString, clickedTimeString,""});
            else
                data.add(new String[]{"", "", clickedString, "",""});

            csv_writer.writeAll(data);

            csv_writer.close();

        } catch(IOException e) {
//            e.printStackTrace();
//            android.util.Log.e(TAG, "exception", e);
        } catch (IndexOutOfBoundsException e2){
//            e2.printStackTrace();
//            android.util.Log.e(TAG, "exception", e2);

        }

    }

    public static void TransportationState_StoreToCSV(long timestamp, String state, String activitySofar){

        String sFileName = "TransportationState.csv"; //Static.csv

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp), timeString, state, String.valueOf(activitySofar)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void dataUploadingCSV(String dataType, String json){

        String sFileName = "DataUploaded.csv"; //Static.csv

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            data.add(new String[]{dataType, json});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }catch (Exception e){
            //e.printStackTrace();
        }
    }
}
