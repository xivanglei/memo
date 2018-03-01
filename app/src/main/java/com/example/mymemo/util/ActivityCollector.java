package com.example.mymemo.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by xianglei on 2018/2/27.
 */

public class ActivityCollector {

    public static void setStatusBarColor(Activity activity, int color) {
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().setStatusBarColor(color);
        } else {
            return;
        }
    }
}
