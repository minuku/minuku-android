package labelingStudy.nctu.minuku_2.controller;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import labelingStudy.nctu.minuku_2.R;

/**
 * Created by Lawrence on 2017/11/8.
 */

public class Timer_site_ListAdapter extends ArrayAdapter<String> {

    private final String TAG = "Timer_site_ListAdapter";

    private Context mContext;

    private ArrayList<String> dataRecords;

    public Timer_site_ListAdapter(Context context, int resource, ArrayList<String> dataRecords) {
        super(context, resource, dataRecords);

        this.mContext = context;

        this.dataRecords = dataRecords;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(TAG, "getView");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.timer_sitelistview, parent, false);

        TextView textView = (TextView) view.findViewById(R.id.ListText);

        if(position==0){
            textView.setText(dataRecords.get(position));
            textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.customize_button));
        }else if(position % 4 == 1){
            textView.setText(dataRecords.get(position));
            textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_yellow));
        }else if(position % 4 == 2){
            textView.setText(dataRecords.get(position));
            textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.reject_button));
        }else if(position % 4 == 3){
            textView.setText(dataRecords.get(position));
            textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.step_pager_previous_tab_color));
        }else if(position % 4 == 0){
            textView.setText(dataRecords.get(position));
//            textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.instabug_annotation_color_yellow));
        }

        return view;
    }
}
