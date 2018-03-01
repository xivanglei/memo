package com.example.mymemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by xianglei on 2018/1/2.
 */

public class MyDatabase extends SQLiteOpenHelper {
    public static  MyDatabase MEMOSQL;
    public static SQLiteDatabase DB;
    public static final String CREATE_MEMO = "create table Memo (" +
            "id integer primary key autoincrement, " +
            "date text, " +
            "content text," +
            "remind_date integer," +
            "voice_dir text," +
            "picture_dir text," +
            "video_dir text," +
            "isRemind integer," +
            "is_lay_up integer)";
    private Context mContext;

    public MyDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory,
                      int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MEMO);
        Toast.makeText(mContext, "创建数据库成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("drop table if exists Memo");
        onCreate(db);
    };
}
