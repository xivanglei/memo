package com.example.mymemo.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by xianglei on 2018/2/28.
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();

    }

    public static Context getContext() {
        return context;
    }
}
