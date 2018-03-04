package com.example.mymemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymemo.util.MediaPlay;
import com.example.mymemo.view.Picture;
import com.example.mymemo.view.PlayVideo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DisplayContent extends AppCompatActivity implements View.OnClickListener {

    private TextView date, remindTime;

    private TextView amendment;

    private EditText content;

    private int id = 0;

    private ImageView displayPicture;

    private ImageButton videoPlay, audioPlay;

    private Button optionRemind;

    private TextView audio;

    private String picturePath, videoPath, audioPath;

    private String mDate, mContent;

    private MediaPlayer mediaPlayer;

    private boolean isAudioPlaying = false;

    private boolean isNotification = false;

    private int mIsRemind;

    private long mRemindDate;

    private static final int UPDATE_AUDIO = 0x11;

    private static final int UPDATE_PICTURE = 0x12;

    private static final int UPDATE_VIDEO = 0x13;

    SimpleDateFormat format = new SimpleDateFormat("MM月dd日 HH:mm");

    public static void actionStart(Context context, int id) {
        Intent intent = new Intent(context, DisplayContent.class);
        intent.putExtra("displayId", id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_content);
        Intent intent = getIntent();
        id = intent.getIntExtra("displayId", 0);
        isNotification = intent.getBooleanExtra("notification", false);
        initView();
        initData();
    }

    public void initView() {
        date = (TextView) findViewById(R.id.display_date);
        date.setOnClickListener(this);
        content = (EditText) findViewById(R.id.display_content);
        remindTime = (TextView) findViewById(R.id.remind_time);
        remindTime.setOnClickListener(this);
        displayPicture = (ImageView) findViewById(R.id.display_picture);
        displayPicture.setOnClickListener(this);
        videoPlay = (ImageButton) findViewById(R.id.video_play);
        videoPlay.setOnClickListener(this);
        videoPlay.setVisibility(View.INVISIBLE);
        audioPlay = (ImageButton) findViewById(R.id.audio_play);
        audioPlay.setOnClickListener(this);
        audioPlay.setVisibility(View.INVISIBLE);
        audio = (TextView) findViewById(R.id.audio);
        audio.setVisibility(View.INVISIBLE);
        optionRemind = (Button) findViewById(R.id.option_remind);
        if(isNotification) {
            optionRemind.setOnClickListener(this);
        } else {
            optionRemind.setVisibility(View.GONE);
        }
        amendment = (TextView) findViewById(R.id.amendment);
        amendment.setOnClickListener(this);
        amendment.setVisibility(View.GONE);
    }

    public void initData() {
        selectSQL();
        if(picturePath != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        Message message = Message.obtain();
                        message.what = UPDATE_PICTURE;
                        handler.sendMessage(message);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setPriority(2);
            thread.start();
        }
        if(videoPath != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        Message message = Message.obtain();
                        message.what = UPDATE_VIDEO;
                        handler.sendMessage(message);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setPriority(2);
            thread.start();
        }
        if(audioPath != null) {
            audioPlay.setVisibility(View.VISIBLE);
            audio.setVisibility(View.VISIBLE);
        }
        this.date.setText(mDate);
        this.content.setText(mContent);
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                amendment.setVisibility(View.VISIBLE);
            }
        });

        if(mIsRemind == 0) {
            remindTime.setText("暂无提醒");
        } else {
            String remindDate = format.format(mRemindDate);
            remindTime.setText("提醒时间: " + remindDate);
        }
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.display_picture:
                Picture.actionStart(this, picturePath);
                break;
            case R.id.video_play:
                PlayVideo.actionStart(this, videoPath);
                break;
            case R.id.audio_play:
                if(!isAudioPlaying) {
                    mediaPlayer = new MediaPlayer();
                    MediaPlay.audioPlay(mediaPlayer, audioPath);
                    isAudioPlaying = true;
                    audioPlay.setImageResource(R.drawable.stop);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            MediaPlay.audioStop(mediaPlayer);
                            isAudioPlaying = false;
                            audioPlay.setImageResource(R.drawable.play);
                        }
                    });
                } else {
                    MediaPlay.audioStop(mediaPlayer);
                    isAudioPlaying = false;
                    audioPlay.setImageResource(R.drawable.play);
                }
                break;
            case R.id.display_date:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.option_remind:
                optionDialog();
                break;
            case R.id.remind_time:
                Remind remind = new Remind(this, id);
                remind.setCancelCallBack(new Remind.CancelCallBack() {
                    @Override
                    public void cancel() {
                    }
                    @Override
                    public void decision(long date) {
                        remindTime.setText("提醒时间: " + format.format(date));
                    }
                });
                break;
            case R.id.amendment:
                Date curDate = new Date(System.currentTimeMillis());
                String newStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curDate);
                String newContent = content.getText().toString();
                MyDatabase.DB.execSQL(String.format("update Memo set date = '%s', content = '%s' where id = %d",
                        newStr, newContent, id));
                finish();
            default:
                break;
        }
    }

    private void selectSQL() {
        Cursor cursor = MyDatabase.DB.rawQuery(String.format("select * from Memo where id = %d", id),
                null);
        if(cursor.moveToNext()) {
            mDate = cursor.getString(cursor.getColumnIndex("date"));
            mContent = cursor.getString(cursor.getColumnIndex("content"));
            picturePath = cursor.getString(cursor.getColumnIndex("picture_dir"));
            videoPath = cursor.getString(cursor.getColumnIndex("video_dir"));
            audioPath = cursor.getString(cursor.getColumnIndex("voice_dir"));
            mRemindDate = cursor.getLong(cursor.getColumnIndex("remind_date"));
            mIsRemind = cursor.getInt(cursor.getColumnIndex("isRemind"));
        }
        cursor.close();
    }

    private void optionRemind(long time) {
        long date = new Date(System.currentTimeMillis()).getTime() + time;
        RemindService.actionStart(this, id, date, mDate, mContent);
        MyDatabase.DB.execSQL(String.format("update Memo set remind_date = %d, isRemind = %d where id = %d",
                date, 1, id));
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_AUDIO:
                    break;
                case UPDATE_PICTURE:
                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                    displayPicture.setImageBitmap(bitmap);
                    break;
                case UPDATE_VIDEO:
                    videoPlay.setVisibility(View.VISIBLE);
                    EditActivity.displayVideoPicture(videoPlay, videoPath);
                    break;
                default:
                    break;
            }
        }
    };

    private void optionDialog() {
        final String[] items = new String[] {"10分钟后提醒！", "30分钟后提醒！",
                "1小时后提醒！", "2小时后提醒！", "完成并删除！"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("是否继续提醒？");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(items[which]) {
                    case "10分钟后提醒！" :
                        optionRemind(10 * 60 * 1000);
                        finish();
                        break;
                    case "30分钟后提醒！" :
                        optionRemind(30 * 60 * 1000);
                        finish();
                        break;
                    case "1小时后提醒！" :
                        optionRemind(60 * 60 * 1000);
                        finish();
                        break;
                    case "2小时后提醒！" :
                        optionRemind(2 * 60 * 60 * 1000);
                        finish();
                        break;
                    case "完成并删除！":
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(DisplayContent.this);
                        SharedPreferences.Editor edit = pref.edit();
                        int quantityCompletion = 0;
                        if(pref != null) {
                            quantityCompletion = pref.getInt("quantity_completion", 0) + 1;
                            Toast.makeText(DisplayContent.this, "您已经完成了" + quantityCompletion + "件事, 请继续努力！",
                                    Toast.LENGTH_SHORT).show();
                            edit.putInt("quantity_completion", quantityCompletion);
                            edit.apply();
                        }
                        MyDatabase.DB.execSQL(String.format("delete from memo where id = %d",
                                id));
                        File file = new File(Directory.getDirectory(DisplayContent.this, id, Directory.DIRECTORY));
                        EditActivity.deleteFile(file);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    public static Intent getIntent(Context context, int id) {
        Intent intent = new Intent(context, DisplayContent.class);
        intent.putExtra("displayId", id);
        return intent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) {
            MediaPlay.audioDestroy(mediaPlayer);
        }
    }
}
