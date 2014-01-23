package com.mynameistodd.autovolume;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {
    private int lastNumber = 0;
    private NumberPicker picker = null;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        picker = new NumberPicker(getContext());

        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        picker.setMaxValue(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        picker.setMinValue(0);
        picker.setValue(lastNumber);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastNumber = picker.getValue();

            if (callChangeListener(lastNumber)) {
                persistInt(lastNumber);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int num = 0;

        if (restoreValue) {
            if (defaultValue == null) {
                num = getPersistedInt(0);
            } else {
                num = getPersistedInt(Integer.parseInt(defaultValue.toString()));
            }
        } else {
            num = Integer.parseInt(defaultValue.toString());
        }

        lastNumber = num;
    }
}
