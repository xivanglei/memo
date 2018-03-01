package com.example.mymemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;

import java.util.Calendar;

/**
 * Created by xianglei on 2018/2/2.
 */

public class DateTimePickerDialog extends AlertDialog {

    private DateTimePicker mDateTimePicker;

    private Calendar mDate;

    private OnDateTimeSetListener mOnDateTimeSetListener;

    public DateTimePickerDialog(Context context, int style) {
        super(context, style);
        mDateTimePicker = new DateTimePicker(context);
        setView(mDateTimePicker);
        mDate = Calendar.getInstance();
        updateTitle();
        mDateTimePicker.setOnDateTimeChangedListener(new DateTimePicker.OnDateTimeChangedListener() {
            @Override
            public void onDateTimeChanged(int year, int month, int day, int hour, int minute) {
                mDate.set(Calendar.YEAR, year);
                mDate.set(Calendar.MONTH, month);
                mDate.set(Calendar.DAY_OF_MONTH, day);
                mDate.set(Calendar.HOUR_OF_DAY, hour);
                mDate.set(Calendar.MINUTE, minute);
                mDate.set(Calendar.SECOND, 0);
                updateTitle();
            }
        });
        setButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.OnDateTimeSet(mDate.getTimeInMillis(), 1);
                }
            }
        });
        setButton2("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.OnDateTimeSet(0, 0);
                }
            }
        });
        setCancelable(false);
    }

    private void updateTitle() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR |
                DateUtils.FORMAT_SHOW_WEEKDAY;
        String title1 = DateUtils.formatDateTime(this.getContext(), mDate.getTimeInMillis(), flags);
        String title2 = DateUtils.formatDateTime(this.getContext(), mDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
        setTitle(title1 + "\n" + title2);
    }

    public interface OnDateTimeSetListener {
        void OnDateTimeSet(long date, int isRemind);
    }

    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        mOnDateTimeSetListener = callBack;
    }
}
