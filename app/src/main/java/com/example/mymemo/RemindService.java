package com.example.mymemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class RemindService extends Service {

    private int id = 0;

    private long remindDate;

    private String creationDate;

    private String content;

    public final String TAG = "RemindService";

    private AlarmManager alarm = null;

    public RemindService() {
    }

    @Override
    public void onCreate() {
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        MyDatabase.MEMOSQL = new MyDatabase(this, "MyMemo", null, 5);
        MyDatabase.DB = MyDatabase.MEMOSQL.getWritableDatabase();
        Cursor cursor = MyDatabase.DB.rawQuery(String.format
                ("select id, date, remind_date, content from Memo where isRemind = 1"), null);
        while(cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex("id"));
            creationDate = cursor.getString(cursor.getColumnIndex("date"));
            content = cursor.getString(cursor.getColumnIndex("content"));
            remindDate = cursor.getLong(cursor.getColumnIndex("remind_date"));
            setRemind();
        }
        cursor.close();
    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if(intent != null) {
            id = intent.getIntExtra("id", 0);
            if(intent.getBooleanExtra("isCancel", false)) {
                alarm.cancel(PendingIntent.getBroadcast(this, id,
                        new Intent("com.example.mymemo.remind"), 0));
            } else if(id != 0) {
                remindDate = intent.getLongExtra("remindDate", 0);
                creationDate = intent.getStringExtra("creationDate");
                content = intent.getStringExtra("content");
                setRemind();
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                String str = format.format(remindDate);
                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void actionStart(Context context, int id, long date, String creationDate, String content) {
        Intent intent = new Intent(context, RemindService.class);
        intent.putExtra("id", id);
        intent.putExtra("remindDate", date);
        intent.putExtra("creationDate", creationDate);
        intent.putExtra("content", content);
        context.startService(intent);
    }

    public static void actionStart(Context context, int id, boolean isCancel) {
        Intent intent = new Intent(context, RemindService.class);
        intent.putExtra("id", id);
        intent.putExtra("isCancel", isCancel);
        context.startService(intent);
    }

    private void setRemind() {
        Intent i = ActionServiceReceiver.getIntent(id, creationDate, content);
        PendingIntent pi = PendingIntent.getBroadcast(this, id, i, 0);
        if(Build.VERSION.SDK_INT >= 23) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, remindDate, pi);
        } else if(Build.VERSION.SDK_INT >= 19){
            alarm.setExact(AlarmManager.RTC_WAKEUP, remindDate, pi);
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, remindDate, pi);
        }
    }
}
