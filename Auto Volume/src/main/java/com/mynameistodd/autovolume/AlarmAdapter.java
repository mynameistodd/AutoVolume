package com.mynameistodd.autovolume;

import android.content.Context;
import android.media.AudioManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * Created by todd on 9/30/13.
 */
public class AlarmAdapter extends ArrayAdapter<Alarm> {

    private Context context;
    private List<Alarm> objects;
    Integer maxVolume;

    public AlarmAdapter(Context context, int resource, List<Alarm> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
        this.maxVolume = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_RING);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.activity_list_alarm_item, null);

        TextView time = (TextView) rowView.findViewById(R.id.tv_time);
        TextView recur = (TextView) rowView.findViewById(R.id.tv_recur);
        TextView volume = (TextView) rowView.findViewById(R.id.tv_volume);
        CompoundButton enabled = (CompoundButton) rowView.findViewById(R.id.switch1);

        final Alarm alarm = objects.get(position);

        //set the time
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        c.set(Calendar.MINUTE, alarm.getMinute());
        time.setText(DateUtils.formatDateTime(context, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));

        //set the recur
        String textToShow = Util.getRecurText(alarm.getRecur());
        recur.setText(textToShow);

        //set the volume
        volume.setText(Util.getVolumePercent(String.valueOf(alarm.getVolume()), maxVolume));

        //set the on/off switch
        enabled.setChecked(alarm.isEnabled());
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    alarm.schedule();
                } else {
                    alarm.cancel();
                }
                alarm.setEnabled(isChecked);
                alarm.save();
            }
        });
        return rowView;
    }
}
