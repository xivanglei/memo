package com.example.mymemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mymemo.MainActivity;
import com.example.mymemo.MyDatabase;
import com.example.mymemo.R;
import com.example.mymemo.dialog.RecordAudioDialog;
import com.example.mymemo.util.ActivityCollector;
import com.example.mymemo.util.MediaPlay;
import com.example.mymemo.view.Picture;
import com.example.mymemo.view.PlayVideo;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "EditActivity";
    private EditText edit;
    private Button add, cancel, eliminate, photoAlbum, takePhoto, recordVideo, recordAudio;
    private ImageView picture;
    private ImageButton videoPlay, audioPlay;
    private String content;
    private SharedPreferences.Editor editor;
    private boolean isAdd = false;
    private String picturePath, audioPath, videoPath;
    private int filedId = 0;
    private MediaPlayer mediaPlayer;
    private boolean isAudioPlaying = false;
    private final int VIDEO = 0x11;
    private final int OPEN_ALBUM = 0x12;
    private final int AUDIO = 0x13;
    private final int CHOOSE_PHOTO = 0x21;
    private final int TAKE_PHOTO = 0x22;
    private final int RECORD_VIDEO = 0x23;
    private File mFile;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Log.d(TAG, "onCreate: ");
        initView();
        initData();
    }

    public void initView() {
        edit = (EditText) findViewById(R.id.edit_content);
        add = (Button) findViewById(R.id.add);
        add.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        eliminate = (Button) findViewById(R.id.eliminate);
        eliminate.setOnClickListener(this);
        photoAlbum = (Button) findViewById(R.id.photo_album);
        photoAlbum.setOnClickListener(this);
        takePhoto = (Button) findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(this);
        recordVideo = (Button) findViewById(R.id.record_video);
        recordVideo.setOnClickListener(this);
        recordAudio = (Button) findViewById(R.id.record_audio);
        recordAudio.setOnClickListener(this);
        picture = (ImageView) findViewById(R.id.picture);
        picture.setEnabled(false);
        picture.setOnClickListener(this);
        videoPlay = (ImageButton) findViewById(R.id.video_play);
        videoPlay.setVisibility(View.INVISIBLE);
        videoPlay.setOnClickListener(this);
        audioPlay = (ImageButton) findViewById(R.id.audio_play);
        audioPlay.setVisibility(View.INVISIBLE);
        audioPlay.setOnClickListener(this);
    }

    public void initData() {
        editor = PreferenceManager.getDefaultSharedPreferences
                (this).edit();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String draft = null;
        if(pref != null) {
            filedId = pref.getInt("filedId", 0) + 1;
            draft = pref.getString("draft", null);
        }
        if(draft != null) {
            edit.setText(draft);
            edit.setSelection(draft.length());
        }
        mFile = new File(Directory.getDirectory(this, filedId, Directory.DIRECTORY));
        if(mFile.exists()) {
            File[] fileArray = mFile.listFiles();
            if (fileArray != null) {
                for (File fileItem : fileArray) {
                    String folder = fileItem.getAbsolutePath().substring(fileItem.getAbsolutePath().lastIndexOf("/") + 1);
                    File[] fileArrayB = fileItem.listFiles();

                    switch (folder) {
                        case Directory.VIDEO_DIRECTORY:
                            if(fileArrayB.length > 0) {
                                videoPath = fileArrayB[0].getAbsolutePath();
                                videoPlay.setVisibility(View.VISIBLE);
                                displayVideoPicture(videoPlay, videoPath);
                            }
                            break;
                        case Directory.AUDIO_DIRECTORY:
                            if(fileArrayB.length > 0) {
                                audioPath = fileArrayB[0].getAbsolutePath();
                                audioPlay.setVisibility(View.VISIBLE);
                            }
                            break;
                        case Directory.PICTURE_DIRECTORY:
                            if(fileArrayB.length > 0) {
                                picturePath = fileArrayB[0].getAbsolutePath();
                                picture.setEnabled(true);
                                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                                picture.setImageBitmap(bitmap);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.add:
                content = edit.getText().toString();
                if(StringUtils.isNotBlank(content)) {
                    editor.putString("draft", "");
                    editor.putInt("filedId", filedId);
                    editor.apply();
                    Date curDate = new Date(System.currentTimeMillis());
                    String newStr = format.format(curDate);
                    MyDatabase.DB.execSQL(String.format("insert into Memo (date, content) values('%s', '%s')",
                            newStr, content));
                    if(picture != null) {
                        MyDatabase.DB.execSQL(String.format("update Memo set picture_dir = '%s' where id = %d",
                                picturePath, filedId));
                    }
                    if(audioPath != null) {
                        MyDatabase.DB.execSQL(String.format("update Memo set voice_dir = '%s' where id = %d",
                                audioPath, filedId));
                    }
                    if(videoPath != null) {
                        MyDatabase.DB.execSQL(String.format("update Memo set video_dir = '%s' where id = %d",
                                videoPath, filedId));
                    }
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    isAdd = true;
                 //   Toast.makeText(this, picturePath + audioPath + videoPath, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "请输入内容！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cancel:
                finish();
                break;

            case R.id.eliminate:
                if(picturePath == null && audioPath == null && videoPath == null && StringUtils.isBlank(edit.getText().toString())) {
                    Toast.makeText(EditActivity.this, "没什么可删的", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialog);
                    dialog.setTitle("清除草稿");
                    dialog.setMessage("将要清楚草稿部分的文稿及媒体文件，清除后无法恢复，别误删咯");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("清除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            edit.setText("");
                            deleteFile(mFile);
                            picturePath = null;
                            audioPath = null;
                            videoPath = null;
                            videoPlay.setVisibility(View.INVISIBLE);
                            picture.setVisibility(View.INVISIBLE);
                            audioPlay.setVisibility(View.INVISIBLE);
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }

                        ;
                    });
                    dialog.show();
                }

                break;

            case R.id.photo_album:
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.
                        WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE}, OPEN_ALBUM);
                } else {
                    openAlbum();
                }
                break;

            case R.id.take_photo:
                List<String> permissionTakePhotoList = new ArrayList<String>();
                String[] permissionsTakePhoto = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA};
                for(String i : permissionsTakePhoto) {
                    if (ContextCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED) {
                        permissionTakePhotoList.add(i);
                    }
                }
                if(permissionTakePhotoList.isEmpty()) {
                    takePhoto();
                } else {
                    permissionsTakePhoto = permissionTakePhotoList.toArray(new String[permissionTakePhotoList.size()]);
                    ActivityCompat.requestPermissions(this, permissionsTakePhoto, TAKE_PHOTO);
                }
                break;

            case R.id.record_video:
                List<String> permissionVideoList = new ArrayList<String>();
                String[] permissionsVideo = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
                for(String i : permissionsVideo) {
                    if (ContextCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED) {
                        permissionVideoList.add(i);
                    }
                }
                if(permissionVideoList.isEmpty()) {
                    Intent intent = RecordVideo.actionStart(this, filedId);
                    startActivityForResult(intent, RECORD_VIDEO);
                } else {
                    permissionsVideo = permissionVideoList.toArray(new String[permissionVideoList.size()]);
                    ActivityCompat.requestPermissions(this, permissionsVideo, VIDEO);
                }
                break;

            case R.id.record_audio:
                List<String> permissionAudioList = new ArrayList<>();
                String[] permissionsAudio = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO};
                for(String i : permissionsAudio) {
                    if(ContextCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED) {
                        permissionAudioList.add(i);
                    }
                }
                if(permissionAudioList.isEmpty()) {
                    recordAudio();
                } else {
                    permissionsAudio = permissionAudioList.toArray(new String[permissionAudioList.size()]);
                    ActivityCompat.requestPermissions(this, permissionsAudio, AUDIO);
                }
                break;

            case R.id.picture:
                Picture.actionStart(this, picturePath);
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

            case R.id.video_play:
                PlayVideo.actionStart(this, videoPath);
                break;

            default:
                break;
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private void takePhoto() {
        picturePath = Directory.getDirectory(this, filedId, Directory.PICTURE_DIRECTORY);
        File outputImage = new File(picturePath);
        try {
            outputImage.createNewFile();
        } catch(IOException e) {
            e.printStackTrace();
        }
        Uri pictureUri = null;
        if(Build.VERSION.SDK_INT >= 24) {
            pictureUri = FileProvider.getUriForFile(this, "com.example.mymemo.fileprovider", outputImage);
        } else {
            pictureUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void recordAudio() {
        RecordAudioDialog recordAudio = new RecordAudioDialog(this, filedId, R.style.AlertDialog);
        recordAudio.setOnDataCallback(new RecordAudioDialog.OnDataCallback() {
            @Override
            public void onCallback(String path) {
                audioPath = path;
                audioPlay.setVisibility(View.VISIBLE);
            }
        });
        recordAudio.show();
    }

    public static void deleteFile(File file) {
        if(file.isDirectory()) {
            File[] arrayFile = file.listFiles();
            if(arrayFile != null) {
                for(File f : arrayFile) {
                    deleteFile(f);
                }
            }
        }
        file.delete();
    }

    public static void displayVideoPicture(ImageButton image, String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(单位为秒)
        int seconds = Integer.valueOf(time) / 1000;
        // 得到每一秒时刻的bitmap比如第一秒,第二秒
        Bitmap bitmap = retriever.getFrameAtTime(2*1000*1000,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        String path = Environment.getExternalStorageDirectory()+ File.separator + 2 + ".jpg";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
          //  drawable.setTileModeXY(Shader.TileMode.REPEAT , Shader.TileMode.REPEAT );
            drawable.setDither(true);
            image.setBackgroundDrawable(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        content = edit.getText().toString();
        if(StringUtils.isBlank(content)) {
            editor.putString("draft", "");
        } else if(!isAdd) {
            editor.putString("draft", content);
        }
        editor.apply();
        if(mediaPlayer != null) {
            MediaPlay.audioDestroy(mediaPlayer);
        }
        Log.d(TAG, "onDestroy: ");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case VIDEO:
                if(grantResults.length > 0) {
                    for(int result: grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能录制视频", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Intent intent = RecordVideo.actionStart(this, filedId);
                    startActivityForResult(intent, RECORD_VIDEO);
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                }
                break;
            case OPEN_ALBUM:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "您取消了存取SD卡权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case TAKE_PHOTO:
                if(grantResults.length > 0) {
                    for(int result: grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能录制视频", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    takePhoto();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                }
                break;

            case AUDIO:
                if(grantResults.length > 0) {
                    for(int result: grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能录音", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    recordAudio();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    picturePath = Directory.getAlbumPath(this, data);
                    if(picturePath != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                        picture.setImageBitmap(bitmap);
                        picture.setEnabled(true);
                    } else {
                        Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                        picture.setImageBitmap(bitmap);
                        picture.setEnabled(true);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case RECORD_VIDEO:
                if(resultCode == RESULT_OK) {
                    videoPath = data.getStringExtra("videoPath");
                    Toast.makeText(this, videoPath, Toast.LENGTH_SHORT).show();
                    videoPlay.setVisibility(View.VISIBLE);
                    displayVideoPicture(videoPlay, videoPath);
                }

            default:
                break;
        }
    }
}
