package com.example.mymemo;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import java.util.Calendar;

/**
 * Created by xianglei on 2018/2/2.
 */

public class DateTimePicker extends FrameLayout {

    private final NumberPicker mDateSpinner;

    private final NumberPicker mHourSpinner;

    private final NumberPicker mMinuteSpinner;

    private Calendar mDate;

    private OnDateTimeChangedListener mOnDateTimeChangedListener;

    public DateTimePicker(final Context context) {
        super(context);
        mDate = Calendar.getInstance();
        inflate(context, R.layout.date_time_picker, this);

        mDateSpinner = (NumberPicker) findViewById(R.id.up_date);
        mDateSpinner.setMinValue(0);
        mDateSpinner.setMaxValue(6);
        updateDateControl();
        mDateSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                mDate.add(Calendar.DAY_OF_YEAR, newVal - oldVal);
                if(mDate.get(Calendar.DAY_OF_YEAR) > Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                    if(DateFormat.is24HourFormat(getContext())) {
                        mHourSpinner.setMinValue(0);
                    } else {
                        mHourSpinner.setMinValue(1);
                    }
                    mHourSpinner.setWrapSelectorWheel(true);
                } else if(mDate.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                    if(DateFormat.is24HourFormat(context)) {
                        mHourSpinner.setMinValue(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                    } else {
                        mHourSpinner.setMinValue(Calendar.getInstance().get(Calendar.HOUR));
                    }
                    mHourSpinner.setWrapSelectorWheel(false);
                }
                updateDateControl();
                onDateTimeChanged();
            }
        });
        mDateSpinner.setWrapSelectorWheel(false);
        mDateSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        mHourSpinner = (NumberPicker) findViewById(R.id.up_hour);

        if(DateFormat.is24HourFormat(context)) {
            mHourSpinner.setMinValue(mDate.get(Calendar.HOUR_OF_DAY));
            mHourSpinner.setMaxValue(23);
            mHourSpinner.setValue(mDate.get(Calendar.HOUR_OF_DAY));
        } else {
            mHourSpinner.setMinValue(mDate.get(Calendar.HOUR));
            mHourSpinner.setMaxValue(12);
            mHourSpinner.setValue(mDate.get(Calendar.HOUR));
        }
        mHourSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                mDate.set(Calendar.HOUR_OF_DAY, newVal);
                if(mDate.get(Calendar.HOUR_OF_DAY) > Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                    mMinuteSpinner.setMinValue(0);
                    mMinuteSpinner.setWrapSelectorWheel(true);
                } else if(mDate.get(Calendar.HOUR_OF_DAY) == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                    mMinuteSpinner.setMinValue(Calendar.getInstance().get(Calendar.MINUTE));
                    mMinuteSpinner.setWrapSelectorWheel(false);
                }
                onDateTimeChanged();
            }
        });
        mHourSpinner.setWrapSelectorWheel(false);
        mHourSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        mMinuteSpinner = (NumberPicker) findViewById(R.id.up_minute);
        mMinuteSpinner.setMinValue(mDate.get(Calendar.MINUTE));
        mMinuteSpinner.setMaxValue(59);
        mMinuteSpinner.setValue(mDate.get(Calendar.MINUTE));
        mMinuteSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                mDate.set(Calendar.MINUTE, newVal);
                onDateTimeChanged();
            }
        });
        mMinuteSpinner.setWrapSelectorWheel(false);
        mMinuteSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

    }

    private void updateDateControl() {
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
        int i = 7;
        String[] dateDisplayValues = new String[i];
        cal.add(Calendar.DAY_OF_YEAR, -7 / 2 - 1);
        mDateSpinner.setDisplayedValues(null);
        for(int a = 0; a < i; a++) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            if(cal.get(Calendar.DAY_OF_YEAR) <  now.get(Calendar.DAY_OF_YEAR)) {
                a--;
                i--;
                dateDisplayValues = new String[i];
            } else {
                String display = (String) DateFormat.format("MM.dd EEEE", cal);
                dateDisplayValues[a] = display;
            }
        }
        if(i < 7) {
            mDateSpinner.setMinValue(7 - i);
        }
        mDateSpinner.setDisplayedValues(dateDisplayValues);
        mDateSpinner.setValue(3);
        mDateSpinner.invalidate();
    }

    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(int year, int month, int day, int hour, int minute);
    }

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callback) {
        mOnDateTimeChangedListener = callback;
    }

    private void onDateTimeChanged() {
        mOnDateTimeChangedListener.onDateTimeChanged(mDate.get(Calendar.YEAR), mDate.get(Calendar.MONTH),
                mDate.get(Calendar.DAY_OF_MONTH), mDate.get(Calendar.HOUR_OF_DAY), mDate.get(Calendar.MINUTE));
    }
}
