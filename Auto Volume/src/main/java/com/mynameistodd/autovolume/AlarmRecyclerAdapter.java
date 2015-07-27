package com.mynameistodd.autovolume;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.List;

/**
 * Created by todd on 1/11/15.
 */
public class AlarmRecyclerAdapter extends RecyclerView.Adapter<AlarmRecyclerAdapter.ViewHolder> {

    Integer mMaxVolume;
    private List<Alarm> mAlarms;
    private Context mContext;
    private IAdapterClicks mListener;
    private FragmentManager fragmentManager;

    public AlarmRecyclerAdapter(Context context, List<Alarm> places, IAdapterClicks listener) {
        this.mContext = context;
        this.mAlarms = places;
        this.mListener = listener;
        this.mMaxVolume = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_RING);
        this.fragmentManager = ((Activity) context).getFragmentManager();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_alarm_item, parent, false);
        return new ViewHolder(view, new ViewHolder.IViewHolderClicks() {
            @Override
            public void onItemClick(int position) {
                mListener.onItemClick(position);
            }
        });
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final Alarm alarm = mAlarms.get(i);

        //set the time
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        c.set(Calendar.MINUTE, alarm.getMinute());
        viewHolder.mTime.setText(DateUtils.formatDateTime(mContext, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
        viewHolder.mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minuteOfDay);

                        alarm.setHour(hourOfDay);
                        alarm.setMinute(minuteOfDay);
                        alarm.save();

                        viewHolder.mTime.setText(DateUtils.formatDateTime(mContext, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
                    }
                }, alarm.getHour(), alarm.getMinute(), false);

                timePickerDialog.show();
            }
        });

        //set the recur
        String textToShow = Util.getRecurText(alarm.getRecur());
        viewHolder.mRecur.setText(textToShow);

        //set the volume
        viewHolder.mVolume.setText(Util.getVolumePercent(String.valueOf(alarm.getVolume()), mMaxVolume));
        viewHolder.mVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new DialogFragment() {

                    SeekBar seekBar;

                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        super.onCreateDialog(savedInstanceState);
                        seekBar = new SeekBar(getActivity());
                        seekBar.setMax(getArguments().getInt("MAX_VOLUME"));
                        seekBar.setProgress(getArguments().getInt("VOLUME"));

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Set Volume")
                                .setPositiveButton("Set",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Log.d(Util.MYNAMEISTODD, "Clicked Done in SetVolume");
                                            }
                                        }
                                )
                                .setView(seekBar);

                        return builder.create();
                    }

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        super.onDismiss(dialog);
                        int seekProgress = seekBar.getProgress();
                        alarm.setVolume(seekProgress);
                        alarm.save();

                        viewHolder.mVolume.setText(Util.getVolumePercent(String.valueOf(alarm.getVolume()), mMaxVolume));
                        Log.d(Util.MYNAMEISTODD, "SetVolume" + seekProgress);
                    }
                };

                Bundle args = new Bundle();
                args.putInt("MAX_VOLUME", mMaxVolume);
                args.putInt("VOLUME", alarm.getVolume());
                dialogFragment.setArguments(args);
                dialogFragment.show(fragmentManager, "volumeDialogFragment");
            }
        });

        //set the on/off switch
        viewHolder.mEnabled.setChecked(alarm.isEnabled());
        viewHolder.mEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

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

        viewHolder.mTitle.setText(alarm.getTitle());

        if (alarm.getType() == Alarm.AlarmType.Calendar) {
            viewHolder.itemView.setAlpha(0.3F);
        }
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
    }

    public interface IAdapterClicks {
        void onItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public IViewHolderClicks mListener;

        public TextView mTime;
        public TextView mRecur;
        public TextView mVolume;
        public CompoundButton mEnabled;
        public TextView mTitle;

        public ViewHolder(View itemView, IViewHolderClicks listener) {
            super(itemView);
            mListener = listener;

            mTime = (TextView) itemView.findViewById(R.id.tv_time);
            mRecur = (TextView) itemView.findViewById(R.id.tv_recur);
            mVolume = (TextView) itemView.findViewById(R.id.tv_volume);
            mEnabled = (CompoundButton) itemView.findViewById(R.id.switch1);
            mTitle = (TextView) itemView.findViewById(R.id.tv_title);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(getPosition());
        }

        public interface IViewHolderClicks {
            void onItemClick(int position);
        }
    }
}
