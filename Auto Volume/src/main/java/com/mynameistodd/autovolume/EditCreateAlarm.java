package com.mynameistodd.autovolume;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//import com.google.analytics.tracking.android.EasyTracker;

public class EditCreateAlarm extends Fragment {

    private Button buttonSave;
    private Button buttonCancel;
    private AudioManager audioManager;
    private AlarmManager alarmManager;
    private static TextView tvTime;
    private static TableRow timeTableRow;
    private static TableRow volumeTableRow;
    private static TableRow recurTableRow;
    private TimePickerDialog tPicker;
    private static Calendar cal;
    private static int id = 0;
    private static int hour = 0;
    private static int minute = 0;
    private static int volume = 0;
    private static boolean enabled = true;
    private Bundle args;
    private TextView tvRecur;
    private Context contextThis;
    private static List<Integer> recurDays;
    private static boolean editMode = false;
    private TextView tvVolume;
    private int maxVolumeStep;
    private int nPickerVal = 0;
    private Alarm alarm;
    EditCreateAlarmCallbacks mCallbacks;

    public interface EditCreateAlarmCallbacks {
        public void onAlarmDismiss();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (EditCreateAlarmCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditCreateAlarmCallbacks");
        }
    }

    public EditCreateAlarm() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contextThis = getActivity();
        audioManager = (AudioManager) contextThis.getSystemService(contextThis.AUDIO_SERVICE);
        alarmManager = (AlarmManager) contextThis.getSystemService(contextThis.ALARM_SERVICE);

        recurDays = new ArrayList<Integer>();
        maxVolumeStep = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        args = getArguments();
        editMode = args.getInt("ID", 0) > 0;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myLayout = inflater.inflate(R.layout.activity_edit_create_alarm, container, false);
        buttonSave = (Button) myLayout.findViewById(R.id.btnSave);
        buttonCancel = (Button) myLayout.findViewById(R.id.btnCancel);
        tvTime = (TextView) myLayout.findViewById(R.id.tvTime);
        timeTableRow = (TableRow) myLayout.findViewById(R.id.timeTableRow);
        volumeTableRow = (TableRow) myLayout.findViewById(R.id.volumeTableRow);
        recurTableRow = (TableRow) myLayout.findViewById(R.id.recurTableRow);
        tvRecur = (TextView) myLayout.findViewById(R.id.tvRecur);
        tvVolume = (TextView) myLayout.findViewById(R.id.tvVolume);

        return myLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        //EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        cal = Calendar.getInstance();

        id = 0;
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        volume = 0;
        enabled = true;
        recurDays.clear();

        if (editMode) {
            alarm = MySQLiteOpenHelper.getAlarm(contextThis, args.getInt("ID"));
            id = alarm.getId();
            hour = alarm.getHour();
            minute = alarm.getMinute();
            volume = alarm.getVolume();
            List<Integer> recurDaysArray = alarm.getRecur();
            enabled = alarm.isEnabled();

            for (int rd : recurDaysArray) {
                recurDays.add(rd);
            }
        } else {
            alarm = new Alarm(id, hour, minute, recurDays, volume, enabled, contextThis);
            if (!recurDays.contains(-1)) {
                recurDays.add(-1);
            }
        }

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        tvTime.setText(DateUtils.formatDateTime(contextThis, cal.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
        tvVolume.setText(Util.getVolumePercent(Integer.toString(volume), maxVolumeStep));
        nPickerVal = volume;

        String textToShow = Util.getRecurText(recurDays);
        tvRecur.setText(textToShow);

        tPicker = new TimePickerDialog(contextThis, new OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {

                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minuteOfDay);

                tvTime.setText(DateUtils.formatDateTime(contextThis, cal.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));

                hour = hourOfDay;
                minute = minuteOfDay;
            }
        }, hour, minute, false);

        buttonSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //Delete old alarm
                if (editMode) {
                    alarm.cancel();
                }

                //Save new alarm
                if (recurDays.size() > 0) {
                    alarm.setHour(hour);
                    alarm.setMinute(minute);
                    alarm.setRecur(recurDays);
                    alarm.setVolume(nPickerVal);
                    alarm.setEnabled(enabled);

                    alarm.schedule();
                    alarm.save();
                }

                mCallbacks.onAlarmDismiss();
            }
        });

        buttonCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCallbacks.onAlarmDismiss();
            }
        });

        timeTableRow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                tPicker.show();
            }
        });

        volumeTableRow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                VolumeDialog volumeDialog = new VolumeDialog();
                Bundle args = new Bundle();
                args.putInt("VOLUME", nPickerVal);
                volumeDialog.setArguments(args);
                volumeDialog.show(getFragmentManager(), "volumeDialog");
            }
        });

        recurTableRow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                RecurDaysDialog recurDaysDialog = new RecurDaysDialog();
                recurDaysDialog.show(getFragmentManager(), "recurDialog");
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        //EasyTracker.getInstance(this).activityStop(this);
    }

    public class RecurDaysDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            final CharSequence[] items = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            boolean[] itemsChecked = new boolean[7];

            if (editMode) {
                for (int rd : recurDays) {
                    if (rd >= 0) {
                        itemsChecked[rd] = true;
                    }
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(contextThis);
            builder.setTitle("Pick recurring days")
                    .setMultiChoiceItems(items, itemsChecked, new OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            //keep track of the selected items
                            if (isChecked) {
                                if (!recurDays.contains(which)) {
                                    recurDays.add(which);
                                    Log.d(Util.MYNAMEISTODD, "Add:" + which);
                                }

                                if (recurDays.contains(-1)) {
                                    recurDays.remove(recurDays.indexOf(-1)); //added a recur day, remove "one time"
                                    Log.d(Util.MYNAMEISTODD, "Remove:-1");
                                }
                            } else {
                                if (recurDays.contains(which)) {
                                    recurDays.remove(recurDays.indexOf(which));
                                    Log.d(Util.MYNAMEISTODD, "Remove:" + which);
                                }
                            }

                            if (recurDays.size() == 0) {
                                recurDays.add(-1);
                                Log.d(Util.MYNAMEISTODD, "Add:-1");
                            }
                        }
                    })
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(Util.MYNAMEISTODD, "Clicked Done in PickDays");
                            if (recurDays.size() == 0) {
                                if (!recurDays.contains(-1)) {
                                    recurDays.add(-1);
                                }
                            }
                        }
                    });

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            tvRecur.setText(Util.getRecurText(recurDays));
        }
    }

    public class VolumeDialog extends DialogFragment {
        SeekBar seekBar;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            seekBar = new SeekBar(contextThis);
            seekBar.setMax(maxVolumeStep);
            seekBar.setProgress(getArguments().getInt("VOLUME"));

            AlertDialog.Builder builder = new AlertDialog.Builder(contextThis);
            builder.setTitle("Set Volume")
                    .setPositiveButton("Set",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Log.d(Util.MYNAMEISTODD, "Clicked Done in SetVolume");
                                }
                            })
                    .setView(seekBar);

            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            nPickerVal = seekBar.getProgress();
            tvVolume.setText(Util.getVolumePercent(Integer.toString(nPickerVal), maxVolumeStep));
            Log.d(Util.MYNAMEISTODD, "SetVolume" + nPickerVal);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.removeItem(R.id.action_add);
        if (editMode) {
            inflater.inflate(R.menu.activity_edit_create_alarm, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_delete:

                //Delete old alarm
                if (editMode) {
                    alarm.delete();
                }

                mCallbacks.onAlarmDismiss();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
