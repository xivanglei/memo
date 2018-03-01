package com.example.mymemo.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mymemo.Directory;
import com.example.mymemo.MyDatabase;
import com.example.mymemo.R;
import com.example.mymemo.util.MediaPlay;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by xianglei on 2018/2/14.
 */

public class RecordAudioDialog extends AlertDialog {

    private MediaRecorder recorder;

    private String path;

    private Context mContext;

    private int id;

    private long time = 0;

    private File[] fileList;

    private File file, newFile;

    private boolean isPlaying = false;

    private MediaPlayer mediaPlayer;

    private OnDataCallback mOnDataCallback;

    public RecordAudioDialog(Context context, int id, int styles) {
        super(context, styles);
        mContext = context;
        if(new File(Directory.getDirectory(mContext, id, Directory.AUDIO_DIRECTORY)).
                getParentFile().listFiles().length > 0) {
            file = new File(Directory.getDirectory(mContext, id, Directory.AUDIO_DIRECTORY)).
                    getParentFile().listFiles()[0];
            fileList = file.getParentFile().listFiles();
        }
        this.id = id;
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        setView(linearLayout);
        final Chronometer ch = new Chronometer(context);
        ch.setGravity(Gravity.CENTER);
        ch.setText(new SimpleDateFormat("ss").format((long) 0));
        ch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                time = SystemClock.elapsedRealtime() - ch.getBase();
                ch.setText(new SimpleDateFormat("ss").format(time));
                if(time >= 60000) {
                    ch.stop();
                }
            }
        });
        linearLayout.addView(ch);
        final ImageButton play = new ImageButton(mContext);
        final Button record = new Button(context);
        if(fileList == null) {
            record.setText("开始讲话");
        } else {
            record.setText("重新录制");
            Toast.makeText(mContext, "已经存在录音提醒", Toast.LENGTH_SHORT).show();
        }
        FrameLayout.LayoutParams recordParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        recordParams.setMargins(0, 40, 0, 40);
        recordParams.gravity = Gravity.RIGHT;
        record.setLayoutParams(recordParams);
        record.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        record.setTextColor(getContext().getResources().getColor((R.color.button)));
        record.getBackground().setAlpha(0);
        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ch.setBase(SystemClock.elapsedRealtime());
                        ch.start();
                        startRecord();
                        record.setText("松开结束");
                        break;
                    case MotionEvent.ACTION_UP:
                        ch.stop();
                        recorder.stop();
                        recorder.release();
                        recorder = null;
                        if(time >= 500) {
                            if(fileList != null) {
                                for(File file : fileList) {
                                    file.delete();
                                }
                            }
                            if(file != null) {
                                file.delete();
                            }
                            file = new File(path);
                            play.setVisibility(View.VISIBLE);
                            record.setText("重新录制");
                        } else {
                            newFile = new File(path);
                            if(newFile.exists()) {
                                newFile.delete();
                            }
                            if(fileList == null) {
                                record.setText("开始说话");
                            } else {
                                record.setText("重新录制");
                            }
                            Toast.makeText(mContext, "录音时间太短", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });
        linearLayout.addView(record);
        play.setImageResource(R.drawable.play);
        play.getBackground().setAlpha(0);
        FrameLayout.LayoutParams playParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        playParams.gravity = Gravity.CENTER_HORIZONTAL ;
        play.setLayoutParams(playParams);
        if(fileList == null) {
            play.setVisibility(View.GONE);
        }
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPlaying) {
                    mediaPlayer = new MediaPlayer();
                    MediaPlay.audioPlay(mediaPlayer, file.getAbsolutePath());
                    isPlaying = true;
                    play.setImageResource(R.drawable.stop);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            MediaPlay.audioStop(mediaPlayer);
                            isPlaying = false;
                            play.setImageResource(R.drawable.play);
                        }
                    });
                } else {
                    MediaPlay.audioStop(mediaPlayer);
                    isPlaying = false;
                    play.setImageResource(R.drawable.play);
                }
            }
        });
        linearLayout.addView(play);
        setTitle("长按录音，最长60秒");
        setButton("储存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(path != null && new File(path).exists()) {
                    if (mOnDataCallback != null) {
                        mOnDataCallback.onCallback(path);
                    }
                    onDestroy();
                } else {
                    Toast.makeText(mContext, "录音不成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
        setButton2("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(path != null) {
                    newFile = new File(path);
                    if (newFile.exists()) {
                        newFile.delete();
                    }
                }
                onDestroy();
            }
        });
    }

    private void startRecord() {
        if(recorder == null) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        }
        try {
            if(file != null) {
                file = file.getParentFile().listFiles()[0];
            }
            path = Directory.getDirectory(mContext, id, Directory.AUDIO_DIRECTORY);
            recorder.setOutputFile(path);
            recorder.prepare();
            recorder.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnDataCallback {
        void onCallback(String path);
    }

    public void setOnDataCallback(OnDataCallback callback) {
        mOnDataCallback = callback;
    }

    public void onDestroy() {
        if(mediaPlayer != null) {
            MediaPlay.audioDestroy(mediaPlayer);
        }
    }
}
