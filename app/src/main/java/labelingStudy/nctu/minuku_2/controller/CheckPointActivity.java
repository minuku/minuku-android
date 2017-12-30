package labelingStudy.nctu.minuku_2.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.CheckpointAndReminderService;

/**
 * Created by Lawrence on 2017/11/8.
 */

public class CheckPointActivity extends AppCompatActivity {

    private final String TAG = "CheckPointActivity";

    private Context mContext;

    private Button checkpoint;

    public CheckPointActivity(){}

    public CheckPointActivity(Context mContext){
        this.mContext = mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkpoint_activity);

    }

    @Override
    public void onResume(){
        super.onResume();

    }

    public void initCheckPoint(View v) {

        checkpoint = (Button) v.findViewById(R.id.check);

        checkpoint.setOnClickListener(checkpointing);
    }

    private Button.OnClickListener checkpointing = new Button.OnClickListener() {
        public void onClick(View v) {
            Log.e(TAG,"checkpointing clicked");

            //for testing the CAR
            CheckpointAndReminderService.CheckpointOrNot = true;
            Toast.makeText(mContext, "Your checkpoint is confirmed !!", Toast.LENGTH_SHORT).show();
        }
    };
}
