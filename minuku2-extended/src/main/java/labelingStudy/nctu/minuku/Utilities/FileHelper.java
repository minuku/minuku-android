package labelingStudy.nctu.minuku.Utilities;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.service.ActivityRecognitionService;
import labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.LocationStreamGenerator;

/**
 * Created by Lawrence on 2018/3/13.
 */

public class FileHelper {

    /** Tag for logging. */
    private static final String LOG_TAG = "FileHelper";

    private static Context mContext;

    private static FileHelper instance;

    public FileHelper(Context context) {

        mContext = context;
    }

    public static FileHelper getInstance(Context context) {
        if(FileHelper.instance == null) {
            try {
                FileHelper.instance = new FileHelper(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return FileHelper.instance;
    }

    public static File getPackageDirectory() {

        return new File (Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
    }


    public static void writeStringToFile(String directory_name, String filename, String content){

        if(isExternalStorageWritable()){
            try{
                File PackageDirectory = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);

                //check whether the project diectory exists
                if(!PackageDirectory.exists()){
                    PackageDirectory.mkdir();
                }

                File directory = new File (Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH + directory_name);

                //check whether the directory exists
                if(!directory.exists()){
                    directory.mkdir();
                }

                String pathfilename = Environment.getExternalStorageDirectory()+ Constants.PACKAGE_DIRECTORY_PATH+directory_name+ filename;
                //Log.d(LOG_TAG, "[writeStringToFile] the file name is " + pathfilename + " the content is " + content);
                File file = new File(pathfilename);
                FileWriter filewriter = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write(content);
                out.close();
            }catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage()+"");

            }
        }

    }


    /**
     * get a string file name from the asset folder
     * @param filename
     * @return
     */
    public static String loadFileFromAsset(String filename) {


        String str = null;
        try {

            InputStream is = mContext.getAssets().open(filename);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            str = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return str;

    }


    public static void readFilesFromInDirectory(String directoryPath) {

        //Get the text file
        File file[] = new File(directoryPath).listFiles();

        int i = 0;
        String filePath="";
        while(i!=file.length){
            filePath = file[i].getAbsolutePath();
            Log.d(LOG_TAG, "[readFilesFromInDirectory] " + i+" " +   filePath);
        }

    }



    /**
     * this function will read files to test the TransportationMode Detection
     */
    public static void readTestFile() {

        //testing postfiles


        String string = loadFileFromAsset("testDataActivity2.csv");
        String[] lines = string.split(System.getProperty("line.separator"));
        for (int i=0; i<lines.length; i++) {


            String[] col = lines[i].split(",");

            //get columns from the transportation mode csv file
            long time = Long.parseLong(col[0]);
            String rec_activitiesStr =  col[1];
            String latest_activitiesStr =  col[2];
            float lat = 0;
            float lng = 0;
            float accuracy =0;

            if (col.length>3){
                String transportationStr = col[3];
                String statusStr = col[4];

                if (!col[5].equals("") && !col[6].equals("") && !col[7].equals("")){
                    lat = Float.parseFloat(col[5]);
                    lng = Float.parseFloat(col[6]);
                    accuracy = Float.parseFloat(col[7]);


                }
            }

//            Log.d(LOG_TAG, "[readTestFile] " + latest_activitiesStr + " : " + " lat:" + lat + " lng " + lng);


            Log.d(LOG_TAG, "[readTestFile] read latest_activitiesStr " + latest_activitiesStr);


            //get activity from the activity string
            String [] activities = latest_activitiesStr.split(";;");
            Log.d(LOG_TAG, "[readTestFile] read activity " + activities);

            List<DetectedActivity> activityList = new ArrayList<DetectedActivity>();

            if (activities!=null){
                for (int j=0; j<activities.length; j++){
                    String activityStr = activities[j].split(":")[0];
                    int confidence = Integer.parseInt(activities[j].split(":")[1]);

                    DetectedActivity activity= new DetectedActivity(
                            ActivityRecognitionStreamGenerator.getActivityTypeFromName(activityStr),confidence);
                    activityList.add(activity);
                    Log.d(LOG_TAG, "[readTestFile] activity " + activity + " : " + confidence);
                }

                //create record for the activity
                ActivityRecognitionDataRecord record = new ActivityRecognitionDataRecord();
                record.setProbableActivities(activityList);
                record.setMostProbableActivity(activityList.get(0));
                // Log.d("ARService", "[test replay] load activity is " + activityList.toString() + "  most probable activit is " + activityList.get(0));
                record.setCreationTime(time);
                Log.d(LOG_TAG, "[readTestFile] readline " + lines[i]);

                //add to the AR service so that we can replay later
                ActivityRecognitionService.addActivityRecognitionRecord(record);
            }


            //create location record for the location
            if (lat!=0 && lng!=0 && accuracy!=0){
                LocationDataRecord locationDataRecord = new LocationDataRecord(
                        lat,
                        lng,
                        accuracy);

                LocationStreamGenerator.addLocationDataRecord(locationDataRecord);
            }



        }

    }


    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }



    public static void recursiveFileFind(File[] file1){
        int i = 0;
        String filePath="";
        if(file1!=null){
            while(i!=file1.length){
                filePath = file1[i].getAbsolutePath();
                if(file1[i].isDirectory()){
                    File file[] = file1[i].listFiles();
                    recursiveFileFind(file);
                }
                i++;
                Log.d(LOG_TAG, "[recursiveFileFind] " + i+" " +   filePath);
            }
        }
    }


}
