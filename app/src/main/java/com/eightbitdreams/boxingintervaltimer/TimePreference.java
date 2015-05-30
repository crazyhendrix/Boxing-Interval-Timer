package com.eightbitdreams.boxingintervaltimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.eightbitdreams.boxingintervaltimer.R;

import java.sql.Time;

/**
 * Created by thon on 29/5/15.
 */
public class TimePreference extends DialogPreference{
    private int lastMinute = 0;
    private int lastSecond = 0;
    private TimePicker picker = null;

    public static int getMinute(String time) {
        String[] pieces = time.split(":");
        return (Integer.parseInt(pieces[0]));
    }

    public static int getSecond(String time) {
        String[] pieces = time.split(":");
        return (Integer.parseInt(pieces[1]));
    }

    public static long getMillis(String time) {
        String[] pieces = time.split(":");
        long millis = ((long) Integer.parseInt(pieces[0]) * 60 * 1000) + ((long) Integer.parseInt(pieces[1]) * 1000);
        return millis;
    }

    public TimePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setPositiveButtonText(R.string.set);
        setNegativeButtonText(R.string.cancel);
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        return (picker);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        picker.setCurrentHour(lastMinute);
        picker.setCurrentMinute(lastSecond);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastMinute = picker.getCurrentHour();
            lastSecond = picker.getCurrentMinute();

            String time = String.valueOf(lastMinute) + ":" + String.valueOf(lastSecond);

            if (callChangeListener(time)) {
                persistString(time);
            }
            super.setSummary(getSummary());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;
        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            time = defaultValue.toString();
        }

        lastMinute = getMinute(time);
        lastSecond = getSecond(time);
    }

    @Override
    public CharSequence getSummary() {
        Time time = new Time(0, lastMinute, lastSecond);
        return String.format("%02d:%02d", time.getMinutes(), time.getSeconds());
    }
}

