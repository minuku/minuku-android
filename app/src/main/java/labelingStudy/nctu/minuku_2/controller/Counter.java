package labelingStudy.nctu.minuku_2.controller;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import labelingStudy.nctu.minuku_2.R;

//import edu.ohio.minuku_2.R;

public class Counter extends AppCompatActivity {

    TextView counter;
    ImageButton play, pause, stop;
    private int tsec=0,csec=0,cmin=0,chour=0;
    private boolean startflag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.counter);

        counter = (TextView)findViewById(R.id.tv_counter);

        play = (ImageButton) findViewById(R.id.btn_play);
        pause = (ImageButton) findViewById(R.id.btn_pause);
        stop = (ImageButton)findViewById(R.id.btn_stop);

        //宣告Timer
        Timer timer01 =new Timer();

        //設定Timer(task為執行內容，0代表立刻開始,間格1秒執行一次)
        timer01.schedule(task, 0,1000);

        //Button監聽
        play.setOnClickListener(listener);
        pause.setOnClickListener(listener);
        stop.setOnClickListener(listener);

    }

    //TimerTask無法直接改變元件因此要透過Handler來當橋樑
    private Handler handler = new Handler(){
        public  void  handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    csec=tsec%60;
                    cmin=tsec/60;
                    chour=tsec/3600;
                    String s="";
                    if(chour <10){
                        s="0"+chour;
                    }else{
                        s=""+chour;
                    }
                    if(cmin <10){
                        s=s+":0"+cmin;
                    }else{
                        s=s+":"+cmin;
                    }
                    if(csec < 10){
                        s=s+":0"+csec;
                    }else{
                        s=s+":"+csec;
                    }

                    //s字串為00:00:00格式
                    counter.setText(s);
                    break;
            }

        }
    };

    private TimerTask task = new TimerTask(){

        @Override
        public void run() {
        // TODO Auto-generated method stub
            if (startflag){
                //如果startflag是true則每秒tsec+1
                tsec++;
                Message message = new Message();

                //傳送訊息1
                message.what =1;
                handler.sendMessage(message);
            }
        }

    };

    private View.OnClickListener listener =new View.OnClickListener(){

        @Override
        public void onClick(View v) {
        // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btn_play:
                    startflag=true;
                    break;
                case R.id.btn_pause:
                    startflag=false;
                    break;
                case R.id.btn_stop:
                    tsec=0;
                    //TextView 初始化
                    counter.setText("00:00:00");
                    break;
            }
        }

    };
}
