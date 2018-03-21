package labelingStudy.nctu.minuku_2.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.controller.CounterActivity.CountFlag;


//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/4/22.
 */

public class Timer_move extends AppCompatActivity {

    final private String TAG = "Timer_move";

    Button walk,bike,car;
    private Button site2;

    public static String TrafficFlag;

    static String BigFlag = "";

    private SharedPreferences sharedPrefs;
    private boolean firstTimeOrNot;


    public Timer_move(){}

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_move);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);
        Log.d(TAG,"firstTimeOrNot : "+ firstTimeOrNot);

        if(firstTimeOrNot) {
            startpermission();
            firstTimeOrNot = false;
            sharedPrefs.edit().putBoolean("firstTimeOrNot", firstTimeOrNot).apply();
        }

        inittimer_move();
    }

    public void startpermission(){
        //Maybe useless in this project.
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));  // 協助工具

        Intent intent1 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  //usage
        startActivity(intent1);

//                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS); //notification
//                    startActivity(intent);

        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//location
    }

    public void inittimer_move(){

        walk = (Button) findViewById(R.id.walk);
        bike = (Button) findViewById(R.id.bike);
        car = (Button) findViewById(R.id.car);

        site2 = (Button) findViewById(R.id.site);
        walk.setOnClickListener(walkingTime);
        bike.setOnClickListener(bikingTime);
        car.setOnClickListener(carTime);

        site2.setOnClickListener(siting);


    }
    //CountFlag: countung situation --- true:stop, false:ongoing
    private ImageButton.OnClickListener bikingTime = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            BigFlag = "move";
            if(!CountFlag && (TrafficFlag.equals("walk") || TrafficFlag.equals("car") || TrafficFlag.equals("site"))) {
                Toast toast = Toast.makeText(Timer_move.this, "You must finish the current situation first : " + TrafficFlag, Toast.LENGTH_LONG);
                toast.show();
            }else{
                TrafficFlag="bike";

                startActivity(new Intent(Timer_move.this, MainActivity.class));
            }

        }
    };

    private ImageButton.OnClickListener carTime = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            BigFlag = "move";

            if(!CountFlag && (TrafficFlag.equals("walk") || TrafficFlag.equals("bike") || TrafficFlag.equals("site"))){
                Toast toast = Toast.makeText(Timer_move.this, "You must finish the current situation first : " + TrafficFlag, Toast.LENGTH_LONG);
                toast.show();
            }else{
                TrafficFlag="car";

                startActivity(new Intent(Timer_move.this, MainActivity.class));
            }

        }
    };

    private ImageButton.OnClickListener walkingTime = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            BigFlag = "move";

            if(!CountFlag && (TrafficFlag.equals("car") || TrafficFlag.equals("bike") || TrafficFlag.equals("site"))){
                Toast toast = Toast.makeText(Timer_move.this, "You must finish the current situation first : " + TrafficFlag, Toast.LENGTH_LONG);
                toast.show();
            }else{
                TrafficFlag="walk";

                startActivity(new Intent(Timer_move.this, MainActivity.class));
            }

        }
    };

    //to view Timer_site
    private Button.OnClickListener siting = new Button.OnClickListener() {
        public void onClick(View v) {
            BigFlag = "site";

            Log.e(TAG,"site clicked");

            if(!CountFlag && (TrafficFlag.equals("walk") || TrafficFlag.equals("bike") || TrafficFlag.equals("car"))){
                Toast toast = Toast.makeText(Timer_move.this, "You must finish the current situation first : " + TrafficFlag, Toast.LENGTH_LONG);
                toast.show();
            }else{
                TrafficFlag="site";

                //TODO this function will increase the screen in stack, need to be optimized.
                startActivity(new Intent(Timer_move.this, Timer_site.class));
            }

        }
    };
}
