package com.example.mymemo.util;

import android.media.MediaPlayer;

import java.io.File;

/**
 * Created by xianglei on 2018/2/18.
 */

public class MediaPlay {

    public static void audioPlay(MediaPlayer mediaPlayer, String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void audioStop(MediaPlayer mediaPlayer) {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }
    }

    public static void audioDestroy(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
