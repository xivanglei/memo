package com.example.mymemo.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.mymemo.R;

import java.io.File;

public class PlayVideo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        Intent intent = getIntent();
        String videoPath = intent.getStringExtra("video_path");
        File file = new File(videoPath);
        VideoView video = (VideoView) findViewById(R.id.video_view);
        MediaController mc = new MediaController(this);
        if(file.exists()) {
            video.setVideoPath(file.getAbsolutePath());
            video.setMediaController(mc);
            video.requestFocus();
            try {
                video.start();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void actionStart(Context context, String path) {
        Intent intent = new Intent(context, PlayVideo.class);
        intent.putExtra("video_path", path);
        context.startActivity(intent);
    }
}
