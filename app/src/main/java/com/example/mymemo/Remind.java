package com.example.mymemo;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.util.Log;


/**
 * Created by xianglei on 2018/2/4.
 */

public class Remind {

    private String creationDate;

    private String content;

    private int id;

    private Context mContext;

    private long remindDate = 0;

    private int isRemind = 0;

    private CancelCallBack cancelCallBack;

    public Remind(Context context, int id) {
        this.id = id;
        Log.d("Remind", "Remind: " + id);
        this.mContext = context;
        Cursor cursor = MyDatabase.DB.rawQuery("select remind_date, content, date from Memo where id = ?", new String[] {id + ""});
        if(cursor.moveToNext()) {
            remindDate = cursor.getLong(cursor.getColumnIndex("remind_date"));
            creationDate = cursor.getString(cursor.getColumnIndex("date"));
            content = cursor.getString(cursor.getColumnIndex("content"));
        }
        cursor.close();
        if(remindDate > System.currentTimeMillis()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("已设置过时间");
            dialog.setMessage("是否使用已经设置的时间");
            dialog.setCancelable(false);
            dialog.setPositiveButton("使用", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MyDatabase.DB.execSQL(String.format("update Memo set isRemind = %d where id = %d",
                            1, Remind.this.id));
                    actionRemind();
                    if(cancelCallBack != null) {
                        cancelCallBack.decision(remindDate);
                    }
                }
            });
            dialog.setNegativeButton("重设", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    setTime();
                }
            });
            dialog.show();
        } else {
            setTime();
        }
    }

    private void save() {
        MyDatabase.DB.execSQL(String.format("update Memo set remind_date = %d, isRemind = %d where id = %d",
                remindDate, isRemind, id));
    }

    private void setTime() {
        DateTimePickerDialog dialog = new DateTimePickerDialog(mContext, R.style.AlertDialog);
        dialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            @Override
            public void OnDateTimeSet(long date, int rm) {
                if(date == 0) {
                    if(cancelCallBack != null) {
                        cancelCallBack.cancel();
                    }
                } else {
                    isRemind = rm;
                    remindDate = date;
                    save();
                    if(cancelCallBack != null) {
                        cancelCallBack.decision(date);
                    }
                    actionRemind();
                }
            }
        });
        dialog.show();
    }

    private void actionRemind() {
      RemindService.actionStart(mContext, id, remindDate, creationDate, content);
    }

    public interface CancelCallBack {
        void cancel();
        void decision(long date);
    }

    public void setCancelCallBack(CancelCallBack callBack) {
        cancelCallBack = callBack;
    }
}
