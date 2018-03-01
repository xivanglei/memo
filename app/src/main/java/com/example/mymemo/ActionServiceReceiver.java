package com.example.mymemo;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.mymemo.util.MyApplication;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ActionServiceReceiver extends BroadcastReceiver {

    int id = 7;

    private Message msg;

    private static final String SERVICE_SEND = "com.example.mymemo.remind";

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if(intent.getAction().equals(SERVICE_SEND)) {
            id = intent.getIntExtra("notifyId", 0);
            Log.d("ActionServiceReceiver", "onReceive: " + id);
            String creationDate = intent.getStringExtra("notifyCreationDate");
            String content = intent.getStringExtra("notifyContent");
            Intent i = DisplayContent.getIntent(context, id);
            i.putExtra("notification", true);
            PendingIntent pi = PendingIntent.getActivity(mContext, id, i, 0);
            NotificationManager manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            Uri sound= Uri.parse("android.resource://com.example.mymemo/" + R.raw.notification);
            Notification notification = new NotificationCompat.Builder(mContext)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.apple)
                    .setContentTitle("别忘了" + creationDate + "的提醒")
                    .setContentText(content)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pi)
                    .setVibrate(new long[] {0, 1000, 1000, 1000, 1000, 1000})
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSound(sound)
                    .build();
            manager.notify(id, notification);
            MyDatabase.DB.execSQL(String.format("update Memo set isRemind = 0 where id = %d", id));
            if (msg != null) {
                msg.send();
            }
        }
    }

    public static Intent getIntent(int id, String creationDate, String content) {
        Log.d("ActionServiceReceiver", "getIntent: " + id);
        Intent intent = new Intent("com.example.mymemo.remind");
        intent.putExtra("notifyId", id);
        intent.putExtra("notifyCreationDate", creationDate);
        intent.putExtra("notifyContent", content);
        return intent;
    }

    public interface Message {
        public void send();
    }

    public void setMessage(Message msg) {
        this.msg = msg;
    }
}
